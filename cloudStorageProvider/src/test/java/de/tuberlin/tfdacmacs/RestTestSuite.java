package de.tuberlin.tfdacmacs;

import de.tuberlin.tfdacmacs.ciphertext.factory.CipherTextTestFactory;
import de.tuberlin.tfdacmacs.crypto.pairing.PairingGenerator;
import de.tuberlin.tfdacmacs.csp.ciphertext.db.CipherTextDB;
import de.tuberlin.tfdacmacs.csp.client.CAClient;
import de.tuberlin.tfdacmacs.csp.files.FileConfiguration;
import de.tuberlin.tfdacmacs.csp.files.FileController;
import de.tuberlin.tfdacmacs.csp.files.db.FileInformationDB;
import de.tuberlin.tfdacmacs.lib.attribute.factory.BasicsGPPTestFactory;
import de.tuberlin.tfdacmacs.lib.gpp.data.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.lib.gpp.events.GlobalPublicParameterChangedEvent;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CloudStorageProviderApplication.class,
        webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class RestTestSuite {

    protected static final String CLIENT_KEYSTORE = "classpath:ca-client-keystore.jks";

    protected TestRestTemplate sslRestTemplate;
    protected TestRestTemplate mutualAuthRestTemplate;

    // Mock beans
    @MockBean
    protected CAClient caClient;

    // Controller
    @Autowired
    protected FileController fileController;

    // Utils and Services
    @Autowired
    protected FileConfiguration fileConfiguration;

    // DBs
    @Autowired
    protected FileInformationDB fileInformationDB;
    @Autowired
    protected CipherTextDB cipherTextDB;

    // Factories
    @Autowired
    protected CipherTextTestFactory cipherTextTestFactory;
    @Autowired
    protected BasicsGPPTestFactory gppTestFactory;

    // Test usages
    @Autowired
    protected ApplicationEventPublisher publisher;
    @Autowired
    protected PairingGenerator pairingGenerator;

    // additional
    @LocalServerPort
    private int localPort;

    // statics

    @PostConstruct
    public void sslTestRestTemplate() {
        mutalAuthenticationRestTemplate(CLIENT_KEYSTORE);
        sslRestTemplate();
    }

    @Before
    public void setupMocks() {
        GlobalPublicParameterDTO globalPublicParameterDTO = gppTestFactory.createDTO();
        doReturn(globalPublicParameterDTO).when(caClient).getGPP();
        publisher.publishEvent(
                new GlobalPublicParameterChangedEvent(globalPublicParameterDTO.toGlobalPublicParameter(pairingGenerator))
        );
    }

    protected void sslRestTemplate() {
        try {
            SSLContext sslContext = SSLContexts
                    .custom()
                    .loadTrustMaterial(ResourceUtils.getFile("classpath:csp-truststore.jks"), "foobar".toCharArray())
                    .build();
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
            HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();
            sslRestTemplate = new TestRestTemplate(new RestTemplateBuilder().rootUri("https://localhost:" + localPort + "/"));
            ((HttpComponentsClientHttpRequestFactory) sslRestTemplate.getRestTemplate().getRequestFactory()).setHttpClient(httpClient);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void mutalAuthenticationRestTemplate(String keystore) {
        try {
            SSLContext sslContext = SSLContexts
                    .custom()
                    .loadTrustMaterial(ResourceUtils.getFile("classpath:csp-truststore.jks"), "foobar".toCharArray())
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

    public void assertSameElements(byte[] actual, byte[] expected) {
        assertThat(actual.length).isEqualTo(expected.length);
        for(int i = 0; i<actual.length; i ++) {
            assertThat(actual[i]).isSameAs(expected[i]);
        }
    }

    @After
    public void cleanUp() {
        fileInformationDB.drop();
        cipherTextDB.drop();
    }
}
