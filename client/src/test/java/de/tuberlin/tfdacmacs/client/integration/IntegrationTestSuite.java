package de.tuberlin.tfdacmacs.client.integration;

import de.tuberlin.tfdacmacs.ClientApplication;
import de.tuberlin.tfdacmacs.client.attribute.db.AttributeDB;
import de.tuberlin.tfdacmacs.client.certificate.db.CertificateDB;
import de.tuberlin.tfdacmacs.client.config.ClientConfig;
import de.tuberlin.tfdacmacs.client.keypair.db.KeyPairDB;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.shell.Shell;
import org.springframework.shell.jline.InteractiveShellApplicationRunner;
import org.springframework.shell.jline.ScriptShellApplicationRunner;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ClientApplication.class,
        properties = {
        ScriptShellApplicationRunner.SPRING_SHELL_SCRIPT_ENABLED + "=false",
        InteractiveShellApplicationRunner.SPRING_SHELL_INTERACTIVE_ENABLED + "=false",

})
@Slf4j
@ActiveProfiles("test")
public abstract class IntegrationTestSuite {

    @Autowired
    protected AttributeDB attributeDB;
    @Autowired
    protected CertificateDB certificateDB;
    @Autowired
    protected KeyPairDB keyPairDB;

    @Autowired
    protected ClientConfig clientConfig;

    @Autowired
    protected Shell shell;


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

    public void evaluate(String command) {
        log.info("executing: {}", command);
        Object ret = shell.evaluate(() -> command);

        if(ret instanceof Exception) {
            throw new RuntimeException((Exception) ret);
        }
    }
}

