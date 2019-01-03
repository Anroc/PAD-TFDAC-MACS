package de.tuberlin.tfdacmacs.crypto.rsa.certificate.factory;

import de.tuberlin.tfdacmacs.crypto.rsa.AsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.StringAsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.certificate.CertificateSigner;
import de.tuberlin.tfdacmacs.crypto.rsa.factory.CertificateRequestFactory;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.assertj.core.util.Lists;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

@Data
@Component
@RequiredArgsConstructor
public class CertificateTestFactory {

    private AsymmetricCryptEngine<?> cryptEngine = new StringAsymmetricCryptEngine();
    private KeyPair keyPair = cryptEngine.generateKeyPair();

    private final CertificateSigner certificateSigner = new CertificateSigner(Lists.newArrayList("localhost"), Lists.newArrayList("127.0.0.1"), 365);
    private final CertificateRequestFactory certificateRequestFactory;

    public X509Certificate createRootCertificate()
            throws CertificateException, CertIOException, OperatorCreationException {
        return createRootCertificate(keyPair, "CN=Central Server,OU=undo.life,O=tu-berlin,L=Berlin,ST=Berlin,C=DE");
    }

    public X509Certificate createRootCertificate(KeyPair keyPair, String subjectDN)
            throws CertIOException, CertificateException, OperatorCreationException {
        Provider bcProvider = new BouncyCastleProvider();
        Security.addProvider(bcProvider);

        long now = System.currentTimeMillis();
        Date startDate = new Date(now);

        X500Name dnName = new X500Name(subjectDN);
        BigInteger certSerialNumber = new BigInteger(Long.toString(now));

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.YEAR, 1);
        Date endDate = calendar.getTime();
        String signatureAlgorithm = "SHA256WithRSA";

        ContentSigner contentSigner = new JcaContentSignerBuilder(signatureAlgorithm).build(keyPair.getPrivate());
        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(dnName, certSerialNumber, startDate, endDate, dnName, keyPair.getPublic());

        BasicConstraints basicConstraints = new BasicConstraints(true); // <-- true for CA, false for EndEntity
        certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true, basicConstraints); // Basic Constraints is usually marked as critical.

        return new JcaX509CertificateConverter().setProvider(bcProvider).getCertificate(certBuilder.build(contentSigner));
    }

    public X509Certificate createEntityCertificate(KeyPair caKeyPair, X509Certificate rootCertificate, String userId, KeyPair clientKeyPair)
            throws IOException, OperatorCreationException, CertificateException, NoSuchAlgorithmException,
            NoSuchProviderException {
        PKCS10CertificationRequest pkcs10CertificationRequest = certificateRequestFactory
                .create(userId, clientKeyPair);

        return certificateSigner
                .sign(pkcs10CertificationRequest, caKeyPair.getPrivate(), rootCertificate, userId,
                        clientKeyPair.getPublic());

    }
}
