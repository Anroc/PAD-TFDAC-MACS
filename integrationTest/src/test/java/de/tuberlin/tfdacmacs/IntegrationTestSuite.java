package de.tuberlin.tfdacmacs;

import de.tuberlin.tfdacmacs.crypto.pairing.PairingGenerator;
import de.tuberlin.tfdacmacs.crypto.rsa.StringAsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.StringSymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.certificate.CertificateUtils;
import de.tuberlin.tfdacmacs.crypto.rsa.factory.CertificateRequestFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.security.KeyPair;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DummySpringBootApplication.class,
        webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class IntegrationTestSuite {

    @Autowired
    protected StringAsymmetricCryptEngine asymmetricCryptEngine;
    @Autowired
    protected StringSymmetricCryptEngine symmetricCryptEngine;
    @Autowired
    protected CertificateRequestFactory certificateRequestFactory;
    @Autowired
    protected PairingGenerator pairingGenerator;

    protected CertificateUtils certificateUtils = new CertificateUtils();

    protected KeyPair clientKeyPair;

    // statics
    protected String email = "test@tu-berlin.de";
    protected String aid = "aa.tu-berlin.de";

    public static final String CA_URL = "https://localhost:9001/";
    public static final String AA_URL = "http://localhost:9002/";

    @PostConstruct
    public void init() {
        clientKeyPair = asymmetricCryptEngine.generateKeyPair();
    }

    protected RestTemplate plainRestTemplate(String rootURL) {
        RestTemplate restTemplate = new RestTemplateBuilder().rootUri(rootURL).build();
        return restTemplate;
    }

    protected RestTemplate sslRestTemplate(String rootURL) {
        try {
            SSLContext sslContext = SSLContexts
                    .custom()
                    .loadTrustMaterial(ResourceUtils.getFile("classpath:ca-truststore.jks"), "foobar".toCharArray())
                    .build();
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
            HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();
            RestTemplate restTemplate = plainRestTemplate(rootURL);
            ((HttpComponentsClientHttpRequestFactory) restTemplate.getRequestFactory()).setHttpClient(httpClient);
            return restTemplate;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected RestTemplate mutalAuthenticationRestTemplate(String rootURL, String keystore) {
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
            RestTemplate restTemplate = plainRestTemplate(rootURL);
            ((HttpComponentsClientHttpRequestFactory) restTemplate.getRequestFactory()).setHttpClient(httpClient);
            return restTemplate;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected HttpHeaders basicAuth() {
        String plainCreds = "admin:password";
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);
        return headers;
    }

    public void assertSameElements(byte[] actual, byte[] expected) {
        assertThat(actual.length).isEqualTo(expected.length);
        for(int i = 0; i<actual.length; i ++) {
            assertThat(actual[i]).isSameAs(expected[i]);
        }
    }
}
