package de.tuberlin.tfdacmacs.centralserver.twofactorkey;

import de.tuberlin.tfdacmacs.RestTestSuite;
import de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.EncryptedTwoFactorKey;
import de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.dto.TwoFactorKeyRequest;
import de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.dto.TwoFactorKeyResponse;
import org.assertj.core.util.Maps;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TwoFactorKeyRestTest extends RestTestSuite {

    private final Map<String, String> encryptedTwoFactorKeys =
            Maps.newHashMap(UUID.randomUUID().toString(), UUID.randomUUID().toString());


    @Test
    public void create() {
        final String userId = UUID.randomUUID().toString();

        TwoFactorKeyRequest twoFactorKeyRequest = new TwoFactorKeyRequest(
                userId,
                encryptedTwoFactorKeys
        );

        ResponseEntity<TwoFactorKeyResponse> exchange = mutualAuthRestTemplate
                .exchange("/two-factor-keys", HttpMethod.POST, new HttpEntity<>(twoFactorKeyRequest),
                        TwoFactorKeyResponse.class);

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.CREATED);
        TwoFactorKeyResponse body = exchange.getBody();
        assertThat(body.getId()).isNotBlank();
        assertThat(body.getEncryptedTwoFactorKeys()).isEqualTo(encryptedTwoFactorKeys);
        assertThat(body.getOwnerId()).isEqualTo(email);
        assertThat(body.getUserId()).isEqualTo(userId);

        assertThat(twoFactorKeyDB.exist(body.getId())).isTrue();
    }

    @Test
    public void getAll() {
        EncryptedTwoFactorKey encryptedTwoFactorKey1 = new EncryptedTwoFactorKey(
                email,
                UUID.randomUUID().toString(),
                encryptedTwoFactorKeys
        );

        EncryptedTwoFactorKey encryptedTwoFactorKey2 = new EncryptedTwoFactorKey(
                UUID.randomUUID().toString(),
                email,
                encryptedTwoFactorKeys
        );

        EncryptedTwoFactorKey encryptedTwoFactorKey3 = new EncryptedTwoFactorKey(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                encryptedTwoFactorKeys
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
        EncryptedTwoFactorKey encryptedTwoFactorKey = new EncryptedTwoFactorKey(
                email,
                UUID.randomUUID().toString(),
                encryptedTwoFactorKeys
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
        assertThat(body.getEncryptedTwoFactorKeys()).isEqualTo(encryptedTwoFactorKey.getEncryptedTwoFactorKeys());
    }

    @Test
    public void update() {
        final Map<String, String> newEncryptedKeys = Maps.newHashMap(
                UUID.randomUUID().toString(), UUID.randomUUID().toString()
        );
        final String userId = UUID.randomUUID().toString();

        EncryptedTwoFactorKey encryptedTwoFactorKey = new EncryptedTwoFactorKey(
                userId,
                email,
                encryptedTwoFactorKeys
        );

        twoFactorKeyDB.insert(encryptedTwoFactorKey);

        TwoFactorKeyRequest twoFactorKeyRequest = new TwoFactorKeyRequest(
                userId,
                newEncryptedKeys
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
        assertThat(body.getEncryptedTwoFactorKeys()).isEqualTo(newEncryptedKeys);

        assertThat(twoFactorKeyDB.findEntity(encryptedTwoFactorKey.getId()).get().getEncryptedTwoFactorKeys())
                .isEqualTo(newEncryptedKeys);
    }

    @Test
    public void remove() {
        EncryptedTwoFactorKey encryptedTwoFactorKey = new EncryptedTwoFactorKey(
                UUID.randomUUID().toString(),
                email,
                encryptedTwoFactorKeys
        );

        twoFactorKeyDB.insert(encryptedTwoFactorKey);

        ResponseEntity<TwoFactorKeyResponse> exchange = mutualAuthRestTemplate
                .exchange("/two-factor-keys/" + encryptedTwoFactorKey.getId(), HttpMethod.DELETE, HttpEntity.EMPTY,
                        TwoFactorKeyResponse.class);

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.NO_CONTENT);
        assertThat(twoFactorKeyDB.exist(encryptedTwoFactorKey.getId())).isFalse();
    }
}
