package de.tuberlin.tfdacmacs.client.integration;

import com.google.common.collect.Sets;
import de.tuberlin.tfdacmacs.client.encrypt.EncryptCommand;
import de.tuberlin.tfdacmacs.client.integration.dto.AttributeValueRequest;
import de.tuberlin.tfdacmacs.client.integration.dto.CreateUserRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class IntegrationTest extends IntegrationTestSuite {

    private static final String email = "bob@tu-berlin.de";
    private static final String testUserEmail = "test@tu-berlin.de";
    private static final String FILE_NAME = "file.dat";
    private static final String FILE_DIR = "./";
    private static final String FILE_PATH = FILE_DIR + FILE_NAME;
    private static final String DECRYPT_DIR = "./decrypted-files";
    private static final byte[] CONTENT = "hello World".getBytes();

    @Autowired
    private EncryptCommand encryptCommand;

    @Before
    public void setup() throws IOException {
        Files.write(Paths.get(FILE_PATH), CONTENT).toFile().deleteOnExit();
    }


    @Test
    public void integrationTest() {
        createUser();

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

        encryptCommand.encrypt(FILE_PATH, false, "(aa.tu-berlin.de.role:student)");

        resetStdStreams();
        evaluate("check");
        assertThat(containsSubSequence(getOutContent(), "aa.tu-berlin.de.role:student")).isTrue();

        evaluate(String.format("decrypt %s 1", DECRYPT_DIR));
        assertThat(Paths.get(DECRYPT_DIR, FILE_NAME))
                .exists()
                .hasBinaryContent(CONTENT);

        evaluate(String.format("2fa trust %s", testUserEmail));

    }

    private void createUser() {
        log.info("Creating new user {}", email);
        HttpHeaders httpHeaders = basicAuthHeader();
        RestTemplate restTemplate = sslRestTemplate(clientConfig.getAaRootUrl());

        CreateUserRequest userCreationRequest = new CreateUserRequest(
                email,
                Sets.newHashSet(new AttributeValueRequest(
                        "aa.tu-berlin.de.role",
                        Sets.newHashSet("student")
                ))
        );

        ResponseEntity<Object> exchange = restTemplate.exchange(
                "/users",
                HttpMethod.POST,
                new HttpEntity<>(userCreationRequest, httpHeaders),
                Object.class
        );

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.CREATED);
    }

    private void approveDevice(String deviceId) {
        log.info("Approve device of user {} for device {}", email, deviceId);
        HttpHeaders httpHeaders = basicAuthHeader();

        RestTemplate restTemplate = sslRestTemplate(clientConfig.getAaRootUrl());

        ResponseEntity<Object> exchange = restTemplate.exchange(
                "/users/" + email + "/approve/" + deviceId,
                HttpMethod.PUT,
                new HttpEntity<>(httpHeaders),
                Object.class
        );

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
    }

    private HttpHeaders basicAuthHeader() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, "Basic " + Base64.encodeBase64String(
                "admin:foobar".getBytes(
                        Charset.forName(Charsets.US_ASCII.name()))));
        return httpHeaders;
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
