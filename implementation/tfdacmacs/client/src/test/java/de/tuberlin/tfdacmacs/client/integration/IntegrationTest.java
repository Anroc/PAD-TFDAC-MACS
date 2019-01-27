package de.tuberlin.tfdacmacs.client.integration;

import de.tuberlin.tfdacmacs.CommandTestSuite;
import de.tuberlin.tfdacmacs.client.encrypt.EncryptCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class IntegrationTest extends CommandTestSuite {

    private static final String email = "test@tu-berlin.de";
    private static final String FILE_NAME = "./file.dat";

    @Autowired
    private EncryptCommand encryptCommand;

    @Override
    public void initMocks() {
        return;
    }

    @Before
    public void setup() throws IOException {
        Files.write(Paths.get(FILE_NAME), "hello World".getBytes()).toFile().deleteOnExit();
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

    @Test
    public void integrationTest() {
        Thread thread = new Thread(
                () -> {
                    sleep(10);
                    approveDevice(certificateDB.find(email).get().getId());
                }
        );

        thread.start();
        evaluate(String.format("register %s", email));

        evaluate("attributes update");
        assertThat(attributeDB.findAll()).isNotEmpty();

        encryptCommand.encrypt(FILE_NAME, null, "(aa.tu-berlin.de.role:student)");
    }

    private void approveDevice(String deviceId) {
        log.info("Approve device of user {} for device {}", email, deviceId);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, "Basic " + Base64.encodeBase64String(
                "admin:foobar".getBytes(
                        Charset.forName(Charsets.US_ASCII.name()))));

        RestTemplate restTemplate = sslRestTemplate(clientConfig.getAaRootUrl());

        ResponseEntity<Object> exchange = restTemplate.exchange(
                "/users/" + email + "/approve/" + deviceId,
                HttpMethod.PUT,
                new HttpEntity<>(httpHeaders),
                Object.class
        );

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
    }

    public static void sleep(long seconds) {
        log.info("waiting for {} seconds", seconds);

        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
