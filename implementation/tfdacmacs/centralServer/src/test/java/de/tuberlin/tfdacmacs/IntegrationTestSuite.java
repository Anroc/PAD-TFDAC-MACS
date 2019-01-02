package de.tuberlin.tfdacmacs;

import de.tuberlin.tfdacmacs.crypto.rsa.StringAsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.basics.gpp.GlobalPublicParameterProvider;
import de.tuberlin.tfdacmacs.centralserver.authority.db.AttributeAuthorityDB;
import de.tuberlin.tfdacmacs.centralserver.certificate.db.CertificateDB;
import de.tuberlin.tfdacmacs.centralserver.certificate.factory.CertificateRequestTestFactory;
import de.tuberlin.tfdacmacs.centralserver.certificate.factory.CertificateUtils;
import de.tuberlin.tfdacmacs.centralserver.config.CredentialConfig;
import de.tuberlin.tfdacmacs.centralserver.gpp.db.GlobalPublicParameterDB;
import de.tuberlin.tfdacmacs.centralserver.gpp.db.GlobalPublicParameterDTODB;
import de.tuberlin.tfdacmacs.centralserver.user.db.UserDB;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CentralServerApplication.class,
        webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class IntegrationTestSuite {

    protected static final String CLIENT_KEYSTORE = "classpath:ca-client-keystore.jks";
    protected static final String AUTHORITY_KEYSTORE = "classpath:ca-authority-keystore.jks";

    protected TestRestTemplate restTemplate;
    protected TestRestTemplate sslRestTemplate;

    // configs
    @Autowired
    protected CredentialConfig credentialConfig;

    // DBs
    @Autowired
    protected CertificateDB certificateDB;
    @Autowired
    protected UserDB userDB;
    @Autowired
    protected AttributeAuthorityDB attributeAuthorityDB;
    @Autowired
    protected GlobalPublicParameterDTODB globalPublicParameterDTODB;
    @Autowired
    protected GlobalPublicParameterDB globalPublicParameterDB;

    // utils
    @Autowired
    protected GlobalPublicParameterProvider gppProvider;
    @Autowired
    protected StringAsymmetricCryptEngine cryptEngine;
    @Autowired
    protected CertificateUtils certificateUtils;

    // test factories
    @Autowired
    protected CertificateRequestTestFactory certificateRequestTestFactory;

    // additional
    @LocalServerPort
    private int localPort;

    @After
    public void cleanUp() {
        certificateDB.drop();
        userDB.drop();
        globalPublicParameterDTODB.drop();
        attributeAuthorityDB.drop();
    }

    @PostConstruct
    public void sslTestRestTemplate() {
        mutalAuthenticationRestTemplate(CLIENT_KEYSTORE);
        sslRestTemplate();
    }

    protected void sslRestTemplate() {
        try {
            SSLContext sslContext = SSLContexts
                    .custom()
                    .loadTrustMaterial(ResourceUtils.getFile("classpath:ca-truststore.jks"), "foobar".toCharArray())
                    .build();
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
            HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();
            restTemplate = new TestRestTemplate(new RestTemplateBuilder().rootUri("https://localhost:" + localPort + "/"));
            ((HttpComponentsClientHttpRequestFactory) restTemplate.getRestTemplate().getRequestFactory()).setHttpClient(httpClient);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void mutalAuthenticationRestTemplate(String keystore) {
        try {
            SSLContext sslContext = SSLContexts
                    .custom()
                    .loadTrustMaterial(ResourceUtils.getFile("classpath:ca-truststore.jks"), "foobar".toCharArray())
                    .loadKeyMaterial(
                            ResourceUtils.getFile(keystore),
                            "foobar".toCharArray(),
                            "foobar".toCharArray()
                    ).build();
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
            HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();
            sslRestTemplate = new TestRestTemplate(
                    new RestTemplateBuilder().rootUri("https://localhost:" + localPort + "/"));
            ((HttpComponentsClientHttpRequestFactory) sslRestTemplate.getRestTemplate().getRequestFactory())
                    .setHttpClient(httpClient);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected HttpHeaders basicAuth() {
        String plainCreds = credentialConfig.getUsername() + ":" + credentialConfig.getPassword();
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);
        return headers;
    }
}
