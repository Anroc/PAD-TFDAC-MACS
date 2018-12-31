package de.tuberlin.tfdacmacs;

import de.tuberlin.tfdacmacs.attributeauthority.attribute.AttributeController;
import de.tuberlin.tfdacmacs.attributeauthority.attribute.db.AttributeDB;
import de.tuberlin.tfdacmacs.attributeauthority.feign.CAClient;
import de.tuberlin.tfdacmacs.attributeauthority.user.db.UserDB;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.AttributeValueKeyGenerator;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.PairingGenerator;
import de.tuberlin.tfdacmacs.basics.factory.AttributeTestFactory;
import de.tuberlin.tfdacmacs.basics.factory.GPPTestFactory;
import de.tuberlin.tfdacmacs.basics.gpp.data.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.basics.gpp.events.GlobalPublicParameterChangedEvent;
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
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import static org.mockito.Mockito.doReturn;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AttributeAuthorityApplication.class,
        webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class IntegrationTestSuite {

    @LocalServerPort
    private int localPort;

    @Autowired
    protected TestRestTemplate restTemplate;

    // Mock beans
    @MockBean
    protected CAClient gppFeignClient;

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
    protected GPPTestFactory gppTestFactory;
    @Autowired
    protected AttributeTestFactory attributeTestFactory;

    // Test usages
    @Autowired
    protected ApplicationEventPublisher publisher;

    @Before
    public void mockGPPRequest() {
        GlobalPublicParameterDTO globalPublicParameterDTO = gppTestFactory.createDTO();
        doReturn(globalPublicParameterDTO).when(gppFeignClient).getGPP();
        publisher.publishEvent(
                new GlobalPublicParameterChangedEvent(globalPublicParameterDTO.toGlobalPublicParameter(pairingGenerator))
        );
    }

    @After
    public void cleanUp() {
        attributeDB.drop();
        userDB.drop();
    }

    @PostConstruct
    public void sslTestRestTemplate()
            throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException,
            UnrecoverableKeyException, KeyManagementException {
        SSLContext sslContext = SSLContexts
                .custom()
                .loadTrustMaterial(ResourceUtils.getFile("classpath:aa-truststore.jks"), "foobar".toCharArray())
                .loadKeyMaterial(ResourceUtils.getFile("classpath:aa-client-keystore.jks"), "foobar".toCharArray(), "foobar".toCharArray())
                .build();
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
        HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();
        restTemplate = new TestRestTemplate(new RestTemplateBuilder().rootUri("https://localhost:" + localPort + "/"));
        ((HttpComponentsClientHttpRequestFactory) restTemplate.getRestTemplate().getRequestFactory()).setHttpClient(httpClient);
    }
}
