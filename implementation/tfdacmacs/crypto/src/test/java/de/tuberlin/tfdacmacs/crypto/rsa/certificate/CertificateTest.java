package de.tuberlin.tfdacmacs.crypto.rsa.certificate;

import de.tuberlin.tfdacmacs.crypto.UnitTestSuite;
import de.tuberlin.tfdacmacs.crypto.rsa.AsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.StringAsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.certificate.factory.CertificateTestFactory;
import de.tuberlin.tfdacmacs.crypto.rsa.factory.CertificateRequestFactory;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static org.assertj.core.api.Assertions.assertThat;

public class CertificateTest extends UnitTestSuite {

    private static X509Certificate rootCertificate;
    private static String trustStoreName = "trustkeystore.jks";
    private static File trustStoreFile;
    private static JavaKeyStore javaKeyStore;

    private static String domain = "localhost";
    private static String ip = "127.0.0.1";
    private static long validForDays = 356;

    private static String commonName = "Central Server";
    private static String subjectDN = "CN=" + commonName + ",OU=undo.life,O=tu-berlin,L=Berlin,ST=Berlin,C=DE";
    private static final AsymmetricCryptEngine<?> cryptEngine = new StringAsymmetricCryptEngine();
    private static final KeyPair caKeyPair = cryptEngine.generateKeyPair();
    private static final CertificateTestFactory certifidacteTestFactory = new CertificateTestFactory(
            new CertificateRequestFactory()
    );
    private static String trustStorePassword = "asdasd";


    private CertificateUtils certificateUtils;

    @BeforeClass
    public static void init() throws CertificateException, IOException, OperatorCreationException, NoSuchAlgorithmException,
            KeyStoreException {
        rootCertificate = certifidacteTestFactory.createRootCertificate(caKeyPair, subjectDN);
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
    }

    @AfterClass
    public static void cleanUp() throws IOException, KeyStoreException {
        javaKeyStore.deleteKeyStore();
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
        String id = "test@tu-berlin.de";
        KeyPair clientKeyPair = cryptEngine.generateKeyPair();

        X509Certificate clientCert = certifidacteTestFactory
                .createEntityCertificate(caKeyPair, rootCertificate, id, clientKeyPair);

        String extractedCN = certificateUtils.extractCommonName(clientCert);
        assertThat(extractedCN).isEqualTo(id);
        certificateUtils.validateCertificate(trustStoreFile, trustStorePassword, clientCert, rootCertificate);
    }
}