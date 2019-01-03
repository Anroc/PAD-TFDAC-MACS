package de.tuberlin.tfdacmacs.crypto.rsa.certificate;

import de.tuberlin.tfdacmacs.crypto.UnitTestSuite;
import de.tuberlin.tfdacmacs.crypto.rsa.AsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.StringAsymmetricCryptEngine;
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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class CertificateTest extends UnitTestSuite {

    private static X509Certificate rootCertificate;
    private static String trustStoreName = "trustkeystore.jks";
    private static File trustStoreFile;
    private static JavaKeyStore javaKeyStore;

    private static String commonName = "Central Server";
    private static String subjectDN = "CN=" + commonName + ",OU=undo.life,O=tu-berlin,L=Berlin,ST=Berlin,C=DE";
    private static final AsymmetricCryptEngine<?> cryptEngine = new StringAsymmetricCryptEngine();
    private static final KeyPair caKeyPair = cryptEngine.generateKeyPair();
    private static String trustStorePassword = "asdasd";

    private String domain = "localhost";
    private String ip = "127.0.0.1";
    private long validForDays = 356;

    private CertificateUtils certificateUtils;
    private CertificateSigner certificateSigner;

    @BeforeClass
    public static void init() throws CertificateException, IOException, OperatorCreationException, NoSuchAlgorithmException,
            KeyStoreException {
        rootCertificate = createRootCertificate(caKeyPair, subjectDN);
        trustStoreFile = Paths.get(trustStoreName).toFile();
        javaKeyStore = new JavaKeyStore(trustStoreName, trustStorePassword);
        javaKeyStore.createEmptyKeyStore(null);
        javaKeyStore.getKeyStore().setEntry("ca", new KeyStore.TrustedCertificateEntry(rootCertificate),
                null);
        javaKeyStore.save(trustStorePassword.toCharArray());
    }


    @Before
    public void setup() {
        this.certificateUtils = new CertificateUtils();
        this.certificateSigner = new CertificateSigner(domain, ip, validForDays);
    }

    @AfterClass
    public static void cleanUp() throws IOException, KeyStoreException {
        javaKeyStore.deleteKeyStore();
    }

    private static X509Certificate createRootCertificate(KeyPair keyPair, String subjectDN)
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

    @Test
    public void extractCommonName_rootCertificate() {
        String extractedCN = certificateUtils.extractCommonName(rootCertificate);
        assertThat(extractedCN).isEqualTo(commonName);
    }

    @Test
    public void validateCertificate_rootCertificate() {
        certificateUtils.validateCertificate(trustStoreFile, trustStorePassword, rootCertificate);
    }

    @Test
    public void createClientCertificate()
            throws IOException, OperatorCreationException, CertificateException, NoSuchAlgorithmException,
            NoSuchProviderException {
        KeyPair clientKeyPair = cryptEngine.generateKeyPair();
        String id = "test@tu-berlin.de";
        PKCS10CertificationRequest pkcs10CertificationRequest = certificateRequestFactory
                .create(id, clientKeyPair);

        X509Certificate clientCert = certificateSigner
                .sign(pkcs10CertificationRequest, caKeyPair.getPrivate(), rootCertificate, id,
                        clientKeyPair.getPublic());

        String extractedCN = certificateUtils.extractCommonName(clientCert);
        assertThat(extractedCN).isEqualTo(id);
        certificateUtils.validateCertificate(trustStoreFile, trustStorePassword, clientCert, rootCertificate);
    }
}