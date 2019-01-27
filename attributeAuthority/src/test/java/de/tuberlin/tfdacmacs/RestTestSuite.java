package de.tuberlin.tfdacmacs;

import de.tuberlin.tfdacmacs.attributeauthority.attribute.AttributeController;
import de.tuberlin.tfdacmacs.attributeauthority.attribute.db.AttributeDB;
import de.tuberlin.tfdacmacs.attributeauthority.attributes.factory.AttributeTestFactory;
import de.tuberlin.tfdacmacs.attributeauthority.authority.db.TrustedAuthorityDB;
import de.tuberlin.tfdacmacs.attributeauthority.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.attributeauthority.certificate.events.RootCertificateRetrieved;
import de.tuberlin.tfdacmacs.attributeauthority.client.CAClient;
import de.tuberlin.tfdacmacs.attributeauthority.config.AttributeAuthorityConfig;
import de.tuberlin.tfdacmacs.attributeauthority.security.config.CredentialConfig;
import de.tuberlin.tfdacmacs.attributeauthority.user.db.UserDB;
import de.tuberlin.tfdacmacs.crypto.pairing.AttributeValueKeyGenerator;
import de.tuberlin.tfdacmacs.crypto.pairing.PairingGenerator;
import de.tuberlin.tfdacmacs.crypto.rsa.certificate.CertificateUtils;
import de.tuberlin.tfdacmacs.crypto.rsa.certificate.JavaKeyStore;
import de.tuberlin.tfdacmacs.crypto.rsa.certificate.factory.CertificateTestFactory;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;
import de.tuberlin.tfdacmacs.lib.attribute.factory.BasicsGPPTestFactory;
import de.tuberlin.tfdacmacs.lib.certificate.data.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.lib.certificate.util.SpringContextAwareCertificateUtils;
import de.tuberlin.tfdacmacs.lib.db.CouchbaseDB;
import de.tuberlin.tfdacmacs.lib.gpp.GlobalPublicParameterProvider;
import de.tuberlin.tfdacmacs.lib.gpp.data.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.lib.gpp.events.GlobalPublicParameterChangedEvent;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
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
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
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

    protected static final String CLIENT_KEYSTORE = "classpath:client-keystore.jks";
    protected static final String AUTHORITY_KEYSTORE = "classpath:authority-keystore.jks";

    protected TestRestTemplate sslRestTemplate;
    protected TestRestTemplate mutualAuthRestTemplate;

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
    @Autowired
    protected TrustedAuthorityDB trustedAuthorityDB;

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
    @Autowired
    protected CredentialConfig credentialConfig;
    @Autowired
    protected ApplicationContext applicationContext;

    // statics
    protected X509Certificate rootCertificate;
    protected KeyPair caKeyPair;
    private File trustStoreFile;
    private String trustStoreName = "truststore.jks";
    private String trustStorePassword = "asdasd";
    private JavaKeyStore javaKeyStore;

    // additional
    @LocalServerPort
    private int localPort;

    @PostConstruct
    public void postConstruct() throws CertificateException, CertIOException, OperatorCreationException {
        this.rootCertificate = certificateTestFactory.createRootCertificate();
        this.caKeyPair = certificateTestFactory.getKeyPair();
    }

    @Before
    public void setupMocks() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
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

    public HttpHeaders basicAuth() {
        HttpHeaders httpHeaders = new HttpHeaders();

        String auth = credentialConfig.getUsername() + ":" + credentialConfig.getPassword();
        byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(Charset.forName("US-ASCII")) );
        String authHeader = "Basic " + new String( encodedAuth );
        httpHeaders.set(HttpHeaders.AUTHORIZATION, authHeader);
        return httpHeaders;
    }

    @PostConstruct
    public void sslTestRestTemplate() {
        mutualAuthenticationRestTemplate(CLIENT_KEYSTORE);
        sslRestTemplate();
    }

    protected void sslRestTemplate() {
        try {
            SSLContext sslContext = SSLContexts
                    .custom()
                    .loadTrustMaterial(ResourceUtils.getFile("classpath:aa-truststore.jks"), "foobar".toCharArray())
                    .build();
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
            HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();
            sslRestTemplate = new TestRestTemplate(new RestTemplateBuilder().rootUri("https://localhost:" + localPort + "/"));
            ((HttpComponentsClientHttpRequestFactory) sslRestTemplate.getRestTemplate().getRequestFactory()).setHttpClient(httpClient);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void mutualAuthenticationRestTemplate(String keystore) {
        try {
            SSLContext sslContext = SSLContexts
                    .custom()
                    .loadTrustMaterial(ResourceUtils.getFile("classpath:aa-truststore.jks"), "foobar".toCharArray())
                    .loadKeyMaterial(
                            ResourceUtils.getFile(keystore),
                            "foobar".toCharArray(),
                            "foobar".toCharArray()
                    ).build();
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
            HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();
            mutualAuthRestTemplate = new TestRestTemplate(
                    new RestTemplateBuilder().rootUri("https://localhost:" + localPort + "/"));
            ((HttpComponentsClientHttpRequestFactory) mutualAuthRestTemplate.getRestTemplate().getRequestFactory())
                    .setHttpClient(httpClient);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @After
    public void cleanUp() throws IOException, KeyStoreException {
        applicationContext.getBeansOfType(CouchbaseDB.class)
                .values()
                .forEach(CouchbaseDB::drop);
        javaKeyStore.deleteKeyStore();
    }
}
