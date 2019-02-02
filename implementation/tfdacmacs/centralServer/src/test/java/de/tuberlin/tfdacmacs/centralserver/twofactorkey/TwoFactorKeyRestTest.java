package de.tuberlin.tfdacmacs.centralserver.twofactorkey;

import de.tuberlin.tfdacmacs.RestTestSuite;
import de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.EncryptedTwoFactorKey;
import de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.dto.TwoFactorKeyRequest;
import de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.dto.TwoFactorKeyResponse;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TwoFactorKeyRestTest extends RestTestSuite {


    @Test
    public void create() {
        final String userId = UUID.randomUUID().toString();
        final String encryptedTwoFactorKey = UUID.randomUUID().toString();

        TwoFactorKeyRequest twoFactorKeyRequest = new TwoFactorKeyRequest(
                userId,
                encryptedTwoFactorKey
        );

        ResponseEntity<TwoFactorKeyResponse> exchange = mutualAuthRestTemplate
                .exchange("/two-factor-keys", HttpMethod.POST, new HttpEntity<>(twoFactorKeyRequest),
                        TwoFactorKeyResponse.class);

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.CREATED);
        TwoFactorKeyResponse body = exchange.getBody();
        assertThat(body.getId()).isNotBlank();
        assertThat(body.getEncryptedTwoFactorKey()).isEqualTo(encryptedTwoFactorKey);
        assertThat(body.getOwnerId()).isEqualTo(email);
        assertThat(body.getUserId()).isEqualTo(userId);

        assertThat(twoFactorKeyDB.exist(body.getId())).isTrue();
    }

    @Test
    public void getAll() {
        final String encryptedKey = UUID.randomUUID().toString();

        EncryptedTwoFactorKey encryptedTwoFactorKey1 = new EncryptedTwoFactorKey(
                email,
                UUID.randomUUID().toString(),
                encryptedKey
        );

        EncryptedTwoFactorKey encryptedTwoFactorKey2 = new EncryptedTwoFactorKey(
                UUID.randomUUID().toString(),
                email,
                encryptedKey
        );

        EncryptedTwoFactorKey encryptedTwoFactorKey3 = new EncryptedTwoFactorKey(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                encryptedKey
        );

        twoFactorKeyDB.insert(encryptedTwoFactorKey1);
        twoFactorKeyDB.insert(encryptedTwoFactorKey2);
        twoFactorKeyDB.insert(encryptedTwoFactorKey3);

        ResponseEntity<List<TwoFactorKeyResponse>> exchange = mutualAuthRestTemplate
                .exchange("/two-factor-keys", HttpMethod.GET, HttpEntity.EMPTY,
                        new ParameterizedTypeReference<List<TwoFactorKeyResponse>>() {});

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(exchange.getBody()).hasSize(2);
    }

    @Test
    public void get() {
        final String encryptedKey = UUID.randomUUID().toString();

        EncryptedTwoFactorKey encryptedTwoFactorKey = new EncryptedTwoFactorKey(
                email,
                UUID.randomUUID().toString(),
                encryptedKey
        );

        twoFactorKeyDB.insert(encryptedTwoFactorKey);

        ResponseEntity<TwoFactorKeyResponse> exchange = mutualAuthRestTemplate
                .exchange("/two-factor-keys/" + encryptedTwoFactorKey.getId(), HttpMethod.GET, HttpEntity.EMPTY,
                        TwoFactorKeyResponse.class);

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        TwoFactorKeyResponse body = exchange.getBody();
        assertThat(body.getId()).isEqualTo(encryptedTwoFactorKey.getId());
        assertThat(body.getUserId()).isEqualTo(encryptedTwoFactorKey.getUserId());
        assertThat(body.getOwnerId()).isEqualTo(encryptedTwoFactorKey.getDataOwnerId());
        assertThat(body.getEncryptedTwoFactorKey()).isEqualTo(encryptedTwoFactorKey.getEncryptedKey());
    }

    @Test
    public void update() {
        final String encryptedKey = UUID.randomUUID().toString();
        final String newEncryptedKey = UUID.randomUUID().toString();
        final String userId = UUID.randomUUID().toString();

        EncryptedTwoFactorKey encryptedTwoFactorKey = new EncryptedTwoFactorKey(
                userId,
                email,
                encryptedKey
        );

        twoFactorKeyDB.insert(encryptedTwoFactorKey);

        TwoFactorKeyRequest twoFactorKeyRequest = new TwoFactorKeyRequest(
                userId,
                newEncryptedKey
        );

        ResponseEntity<TwoFactorKeyResponse> exchange = mutualAuthRestTemplate
                .exchange("/two-factor-keys/" + encryptedTwoFactorKey.getId(),
                        HttpMethod.PUT,
                        new HttpEntity<>(twoFactorKeyRequest),
                        TwoFactorKeyResponse.class);

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        TwoFactorKeyResponse body = exchange.getBody();
        assertThat(body.getId()).isEqualTo(encryptedTwoFactorKey.getId());
        assertThat(body.getUserId()).isEqualTo(encryptedTwoFactorKey.getUserId());
        assertThat(body.getOwnerId()).isEqualTo(encryptedTwoFactorKey.getDataOwnerId());
        assertThat(body.getEncryptedTwoFactorKey()).isEqualTo(newEncryptedKey);

        assertThat(twoFactorKeyDB.findEntity(encryptedTwoFactorKey.getId()).get().getEncryptedKey())
                .isEqualTo(newEncryptedKey);
    }

    @Test
    public void remove() {
        final String encryptedKey = UUID.randomUUID().toString();

        EncryptedTwoFactorKey encryptedTwoFactorKey = new EncryptedTwoFactorKey(
                UUID.randomUUID().toString(),
                email,
                encryptedKey
        );

        twoFactorKeyDB.insert(encryptedTwoFactorKey);

        ResponseEntity<TwoFactorKeyResponse> exchange = mutualAuthRestTemplate
                .exchange("/two-factor-keys/" + encryptedTwoFactorKey.getId(), HttpMethod.DELETE, HttpEntity.EMPTY,
                        TwoFactorKeyResponse.class);

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.NO_CONTENT);
        assertThat(twoFactorKeyDB.exist(encryptedTwoFactorKey.getId())).isFalse();
    }
}
