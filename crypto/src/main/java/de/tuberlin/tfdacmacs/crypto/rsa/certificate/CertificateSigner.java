package de.tuberlin.tfdacmacs.crypto.rsa.certificate;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Slf4j
@Data
@RequiredArgsConstructor
public class CertificateSigner {

    private final List<String> domains;
    private final List<String> ips;
    private final long validForDays;

    public X509Certificate sign(@NonNull PKCS10CertificationRequest inputCSR, @NonNull PrivateKey caPrivate,
            @NonNull X509Certificate caCertificate, @NonNull String id, @NonNull PublicKey clientKey)
            throws NoSuchProviderException, IOException, OperatorCreationException, CertificateException,
            NoSuchAlgorithmException {
        log.info("Creating certificate for user {}", id);
        AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA512withRSA");
        AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);

        AsymmetricKeyParameter caKeyParameter = PrivateKeyFactory.createKey(caPrivate.getEncoded());
        SubjectPublicKeyInfo keyInfo = SubjectPublicKeyInfo.getInstance(clientKey.getEncoded());

        PKCS10CertificationRequest pk10Holder = new PKCS10CertificationRequest(inputCSR.toASN1Structure());

        List<GeneralName> generalNames = new ArrayList<>();
        getDomains().stream().map(domainName -> new GeneralName(GeneralName.dNSName, domainName)).forEach(generalNames::add);
        getIps().stream().map(ip -> new GeneralName(GeneralName.iPAddress, ip)).forEach(generalNames::add);
        GeneralNames subjectAltName = new GeneralNames(generalNames.toArray(new GeneralName[]{}));

        JcaX509ExtensionUtils jcaX509ExtensionUtils = new JcaX509ExtensionUtils();
        AuthorityKeyIdentifier authorityKeyIdentifier = jcaX509ExtensionUtils.createAuthorityKeyIdentifier(caCertificate);
        SubjectKeyIdentifier subjectKeyIdentifier = jcaX509ExtensionUtils.createSubjectKeyIdentifier(
                SubjectPublicKeyInfo.getInstance(clientKey.getEncoded()));

        X509v3CertificateBuilder myCertificateGenerator = new X509v3CertificateBuilder(
                X500Name.getInstance(caCertificate.getSubjectX500Principal().getEncoded()),
                new BigInteger(32, new Random()),
                new Date(),
                new Date(Instant.now().plus(getValidForDays(), ChronoUnit.DAYS).toEpochMilli()),
                pk10Holder.getSubject(),
                keyInfo);
        myCertificateGenerator.addExtension(new Extension(
                Extension.subjectAlternativeName,
                false,
                new DEROctetString(subjectAltName)
        ));
        myCertificateGenerator.addExtension(new Extension(
                Extension.authorityKeyIdentifier,
                false,
                new DEROctetString(authorityKeyIdentifier)
        ));
        myCertificateGenerator.addExtension(new Extension(
                Extension.subjectKeyIdentifier,
                false,
                new DEROctetString(subjectKeyIdentifier)
        ));


        ContentSigner sigGen = new BcRSAContentSignerBuilder(sigAlgId, digAlgId)
                .build(caKeyParameter);

        X509CertificateHolder holder = myCertificateGenerator.build(sigGen);
        Certificate eeX509CertificateStructure = holder.toASN1Structure();

        CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
        // Read Certificate
        InputStream is1 = new ByteArrayInputStream(eeX509CertificateStructure.getEncoded());
        X509Certificate theCert = (X509Certificate) cf.generateCertificate(is1);
        is1.close();
        log.info("Generated certificate for user {}", id);
        return theCert;
    }
}
