package de.tuberlin.tfdacmacs;

import de.tuberlin.tfdacmacs.attributeauthority.attribute.AttributeController;
import de.tuberlin.tfdacmacs.attributeauthority.attribute.db.AttributeDB;
import de.tuberlin.tfdacmacs.attributeauthority.attributes.factory.AttributeTestFactory;
import de.tuberlin.tfdacmacs.attributeauthority.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.attributeauthority.client.CAClient;
import de.tuberlin.tfdacmacs.attributeauthority.config.AttributeAuthorityConfig;
import de.tuberlin.tfdacmacs.attributeauthority.factory.BasicsGPPTestFactory;
import de.tuberlin.tfdacmacs.attributeauthority.init.certificate.events.RootCertificateRetrieved;
import de.tuberlin.tfdacmacs.attributeauthority.user.db.UserDB;
import de.tuberlin.tfdacmacs.crypto.pairing.AttributeValueKeyGenerator;
import de.tuberlin.tfdacmacs.crypto.pairing.PairingGenerator;
import de.tuberlin.tfdacmacs.crypto.rsa.certificate.CertificateUtils;
import de.tuberlin.tfdacmacs.crypto.rsa.certificate.JavaKeyStore;
import de.tuberlin.tfdacmacs.crypto.rsa.certificate.factory.CertificateTestFactory;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;
import de.tuberlin.tfdacmacs.lib.certificate.data.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.lib.certificate.util.SpringContextAwareCertificateUtils;
import de.tuberlin.tfdacmacs.lib.gpp.GlobalPublicParameterProvider;
import de.tuberlin.tfdacmacs.lib.gpp.data.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.lib.gpp.events.GlobalPublicParameterChangedEvent;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AttributeAuthorityApplication.class,
        webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class RestTestSuite {

    @Autowired
    protected TestRestTemplate restTemplate;

    // Mock beans
    @MockBean
    protected CAClient caClient;

    // Controller
    @Autowired
    protected AttributeController attributeController;

    // Utils and Services
    @Autowired
    protected PairingGenerator pairingGenerator;
    @Autowired
    protected AttributeValueKeyGenerator attributeValueKeyGenerator;

    // DBs
    @Autowired
    protected AttributeDB attributeDB;
    @Autowired
    protected UserDB userDB;

    // Factories
    @Autowired
    protected BasicsGPPTestFactory gppTestFactory;
    @Autowired
    protected AttributeTestFactory attributeTestFactory;
    @Autowired
    protected CertificateTestFactory certificateTestFactory;

    // Test usages
    @Autowired
    protected ApplicationEventPublisher publisher;
    @Autowired
    protected AttributeAuthorityConfig attributeAuthorityConfig;
    @SpyBean
    protected SpringContextAwareCertificateUtils certificateUtils;
    @Autowired
    protected GlobalPublicParameterProvider globalPublicParameterProvider;

    // statics
    protected X509Certificate rootCertificate;
    protected KeyPair caKeyPair;
    private File trustStoreFile;
    private String trustStoreName = "truststore.jks";
    private String trustStorePassword = "asdasd";
    private JavaKeyStore javaKeyStore;

    @PostConstruct
    public void postConstruct() throws CertificateException, CertIOException, OperatorCreationException {
        this.rootCertificate = certificateTestFactory.createRootCertificate();
        this.caKeyPair = certificateTestFactory.getKeyPair();
    }

    @Before
    public void mockGPPRequest() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        GlobalPublicParameterDTO globalPublicParameterDTO = gppTestFactory.createDTO();
        doReturn(globalPublicParameterDTO).when(caClient).getGPP();
        publisher.publishEvent(
                new GlobalPublicParameterChangedEvent(globalPublicParameterDTO.toGlobalPublicParameter(pairingGenerator))
        );

        CertificateResponse rootCertificateResponse = new CertificateResponse(
                "root",
                KeyConverter.from(rootCertificate).toBase64());
        doReturn(rootCertificateResponse).when(caClient).getCentralAuthorityCertificate();
        publisher.publishEvent(
                new RootCertificateRetrieved(new Certificate(rootCertificateResponse.getId(), rootCertificate))
        );

        trustStoreFile = Paths.get(trustStoreName).toFile();
        javaKeyStore = new JavaKeyStore(trustStoreName, trustStorePassword);
        javaKeyStore.createEmptyKeyStore(null);
        javaKeyStore.getKeyStore().setEntry("ca", new KeyStore.TrustedCertificateEntry(rootCertificate),
                null);
        javaKeyStore.save(trustStorePassword.toCharArray());

        Answer answer = invocation -> {
            new CertificateUtils().validateCertificate(trustStoreFile, trustStorePassword, invocation.getArgument(0),
                    new X509Certificate[] {
                      invocation.getArgument(1)
                    }
            );
            return null;
        };
        doAnswer(answer).when(certificateUtils).validateCertificate(any(X509Certificate.class), any(X509Certificate.class));
    }

    public void assertSameElements(byte[] actual, byte[] expected) {
        assertThat(actual.length).isEqualTo(expected.length);
        for(int i = 0; i<actual.length; i ++) {
            assertThat(actual[i]).isSameAs(expected[i]);
        }
    }

    @After
    public void cleanUp() throws IOException, KeyStoreException {
        attributeDB.drop();
        userDB.drop();
        javaKeyStore.deleteKeyStore();
    }
}
