package de.tuberlin.tfdacmacs.centralserver.certificates;

import de.tuberlin.tfdacmacs.centralserver.config.KeyStoreConfig;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.pkcs.CertificationRequest;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class CertificateService {

    private final JavaKeyStore javaKeyStore;
    private final KeyStoreConfig keyStoreConfig;

    public X509Certificate certificateRequest(
            @NonNull String id,
            @NonNull PKCS10CertificationRequest certificateRequest,
            @NonNull PublicKey publicKey) {
        try {
            PrivateKey key = (PrivateKey) javaKeyStore.getKeyEntry(keyStoreConfig.getCaAlias(), keyStoreConfig.getKeyPassword());
            return sign(certificateRequest.toASN1Structure(), key, id, publicKey);
        } catch (UnrecoverableEntryException | NoSuchAlgorithmException | KeyStoreException | NoSuchProviderException | IOException | OperatorCreationException | CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    public X509Certificate sign(CertificationRequest inputCSR, PrivateKey caPrivate, String id, PublicKey clientKey)
            throws NoSuchProviderException, IOException, OperatorCreationException, CertificateException {
        log.info("Creating certificate for user {}", id);
        AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA512withRSA");
        AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);

        AsymmetricKeyParameter caKeyParameter = PrivateKeyFactory.createKey(caPrivate.getEncoded());
        SubjectPublicKeyInfo keyInfo = SubjectPublicKeyInfo.getInstance(clientKey.getEncoded());

        PKCS10CertificationRequest pk10Holder = new PKCS10CertificationRequest(inputCSR);

        GeneralNames subjectAltName = new GeneralNames(
                new GeneralName[]{
                        new GeneralName(GeneralName.dNSName, "localhost"),
                        new GeneralName(GeneralName.iPAddress, "127.0.0.1")
                });

        X509v3CertificateBuilder myCertificateGenerator = new X509v3CertificateBuilder(
                new X500Name(String.format(
                        "CN=%s,OU=undo.life,O=tu-berlin,L=Berlin,ST=Berlin,C=DE", id)),
                new BigInteger("1"),
                new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis() + 30 * 365 * 24 * 60 * 60 * 1000),
                pk10Holder.getSubject(),
                keyInfo);
        myCertificateGenerator.addExtension(new Extension(
                Extension.subjectAlternativeName,
                false,
                new DEROctetString(subjectAltName)
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
