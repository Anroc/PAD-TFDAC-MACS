package de.tuberlin.tfdacmacs.client.integration;

import com.google.common.collect.Sets;
import de.tuberlin.tfdacmacs.client.decrypt.DecryptionCommand;
import de.tuberlin.tfdacmacs.client.encrypt.EncryptCommand;
import de.tuberlin.tfdacmacs.client.integration.dto.AttributeValueRequest;
import de.tuberlin.tfdacmacs.client.integration.dto.CreateUserRequest;
import de.tuberlin.tfdacmacs.crypto.pairing.data.CipherText;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

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

    @Value("${client.test.create-user:true}")
    public boolean createUser;

    @Autowired
    private EncryptCommand encryptCommand;
    @Autowired
    private DecryptionCommand decryptionCommand;

    @Before
    public void setup() throws IOException {
        Files.write(Paths.get(FILE_PATH), CONTENT).toFile().deleteOnExit();

        // 1. delete from `ca-integration-test` where _class = "de.tuberlin.tfdacmacs.centralserver.ciphertext.data.CipherTextEntity";
        // 2. delete from `ca-integration-test` where _class = "de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.EncryptedTwoFactorKey";
    }


    @Test
    public void integrationTest() throws IOException {
        if(createUser) {
            createUser();
        } else {
            log.info("Not creating user since client.test.create-user={}", createUser);
        }

        Thread thread = new Thread(
                () -> {
                    sleep(10);
                    approveDevice(certificateDB.find(email).get().getId());
                }
        );

        thread.start();
        log.info("Starting register of user {}", email);
        evaluate(String.format("register %s", email));

        evaluate("attributes update");
        assertThat(attributeDB.findAll()).isNotEmpty();

        encryptDecrypt_without_2FA();

        // --------------- 2 FA Test ------------------

        FileUtils.cleanDirectory(Paths.get(DECRYPT_DIR).toFile());
        encryptDecrypt_with_2FA_trustedMyselfAndTestUser();

        FileUtils.cleanDirectory(Paths.get(DECRYPT_DIR).toFile());
        revoke2FA_passes_andUpdatesCT();

        revoke2FA_passes_onRevokingMySelf();


        // --- attribute revocation test ---
        FileUtils.cleanDirectory(Paths.get(DECRYPT_DIR).toFile());

        revokeAccess_fromTestUser();
    }

    private void revokeAccess_fromTestUser() {
        revokeAttribute(testUserEmail, "aa.tu-berlin.de.role:student");

        resetStdStreams();
        evaluate("check");
        assertThat(containsSubSequence(getOutContent(), "aa.tu-berlin.de.role:student")).isFalse();

        evaluate("attributes update");

        resetStdStreams();
        evaluate("check");
        assertThat(containsSubSequence(getOutContent(), "aa.tu-berlin.de.role:student")).isTrue();

        evaluate(String.format("decrypt %s 1", DECRYPT_DIR));
        assertThat(Paths.get(DECRYPT_DIR, FILE_NAME))
                .exists()
                .hasBinaryContent(CONTENT);
    }

    private void revoke2FA_passes_onRevokingMySelf() {
        evaluate(String.format("2fa distrust %s", email));
        evaluate("2fa update");

        resetStdStreams();
        evaluate("2fa list --issued");
        assertThat(containsSubSequence(getOutContent(), email)).isFalse();
        assertThat(containsSubSequence(getOutContent(), testUserEmail)).isFalse();
        resetStdStreams();
        evaluate("2fa list --granted");
        assertThat(containsSubSequence(getOutContent(), email)).isFalse();

        resetStdStreams();
        evaluate("check");
        assertThat(containsSubSequence(getOutContent(), "yes (by VersionedID(id=" + email + ", version=0))\t[VersionedID(id=aa.tu-berlin.de.role:student, version=0)")).isFalse();
    }

    private void revoke2FA_passes_andUpdatesCT() {
        evaluate(String.format("2fa distrust %s", testUserEmail));
        evaluate("2fa update");

        resetStdStreams();
        evaluate("2fa list --issued");
        assertThat(containsSubSequence(getOutContent(), email)).isTrue();
        assertThat(containsSubSequence(getOutContent(), testUserEmail)).isFalse();
        resetStdStreams();
        evaluate("2fa list --granted");
        assertThat(containsSubSequence(getOutContent(), email)).isTrue();

        resetStdStreams();
        evaluate("check");
        assertThat(containsSubSequence(getOutContent(), "yes (by VersionedID(id=" + email + ", version=1))\t[VersionedID(id=aa.tu-berlin.de.role:student, version=0)")).isTrue();

        int numberOf2FACipherText = find2FACipherTextNumber();
        evaluate(String.format("decrypt %s %d", DECRYPT_DIR, numberOf2FACipherText));
        assertThat(Paths.get(DECRYPT_DIR, FILE_NAME))
                .exists()
                .hasBinaryContent(CONTENT);
    }

    private void encryptDecrypt_without_2FA() {
        encryptCommand.encrypt(FILE_PATH, false, "(aa.tu-berlin.de.role:student)");

        resetStdStreams();
        evaluate("check");
        assertThat(containsSubSequence(getOutContent(), "aa.tu-berlin.de.role:student")).isTrue();

        evaluate(String.format("decrypt %s 1", DECRYPT_DIR));
        assertThat(Paths.get(DECRYPT_DIR, FILE_NAME))
                .exists()
                .hasBinaryContent(CONTENT);
    }

    private void encryptDecrypt_with_2FA_trustedMyselfAndTestUser() {
        // trust myself so that i can use 2fa to decrypt the cipher text
        evaluate(String.format("2fa trust %s,%s", email, testUserEmail));
        evaluate("2fa update");

        resetStdStreams();
        evaluate("2fa list --issued");
        assertThat(containsSubSequence(getOutContent(), email)).isTrue();
        assertThat(containsSubSequence(getOutContent(), testUserEmail)).isTrue();
        resetStdStreams();
        evaluate("2fa list --granted");
        assertThat(containsSubSequence(getOutContent(), email)).isTrue();

        encryptCommand.encrypt(FILE_PATH, true, "(aa.tu-berlin.de.role:student)");

        resetStdStreams();
        evaluate("check");
        assertThat(containsSubSequence(getOutContent(), "yes (by VersionedID(id=" + email + ", version=0))\t[VersionedID(id=aa.tu-berlin.de.role:student, version=0)")).isTrue();

        int numberOf2FACipherText = find2FACipherTextNumber();
        evaluate(String.format("decrypt %s %d", DECRYPT_DIR, numberOf2FACipherText));
        assertThat(Paths.get(DECRYPT_DIR, FILE_NAME))
                .exists()
                .hasBinaryContent(CONTENT);
    }

    private int find2FACipherTextNumber() {
        List<CipherText> cipherTexts = decryptionCommand.getCipherTexts();
        for (int i = 0; i < cipherTexts.size(); i++) {
            if(cipherTexts.get(i).isTwoFactorSecured())
                return i + 1;
        }

        throw new IllegalStateException("No cipher text with 2FA secure found.");
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

    private void revokeAttribute(String attributeValueId, String userId) {
        log.info("Revoking attribute {} from {} ", attributeValueId, userId);
        HttpHeaders httpHeaders = basicAuthHeader();

        String[] attribute = attributeValueId.split(":");

        RestTemplate restTemplate = sslRestTemplate(clientConfig.getAaRootUrl());

        ResponseEntity<Object> exchange = restTemplate.exchange(
                "/users/" + userId + "/attributes/" + attribute[0] + "/values/" + attribute[1],
                HttpMethod.DELETE,
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
