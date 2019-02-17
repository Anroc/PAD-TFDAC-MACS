package de.tuberlin.tfdacmacs.centralserver.twofactorkey;

import de.tuberlin.tfdacmacs.RestTestSuite;
import de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.EncryptedTwoFactorDeviceKey;
import de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.EncryptedTwoFactorKey;
import de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.dto.EncryptedTwoFactorDeviceKeyDTO;
import de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.dto.TwoFactorKeyRequest;
import de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.dto.TwoFactorKeyResponse;
import de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.dto.TwoFactorUpdateKeyRequest;
import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import it.unisa.dia.gas.jpbc.Element;
import org.assertj.core.util.Maps;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class TwoFactorKeyRestTest extends RestTestSuite {

    private Map<String, EncryptedTwoFactorDeviceKeyDTO> encryptedTwoFactorKeys =
            Maps.newHashMap(UUID.randomUUID().toString(),
                    new EncryptedTwoFactorDeviceKeyDTO(
                            UUID.randomUUID().toString(),
                            UUID.randomUUID().toString()
                    ));
    private Map<String, EncryptedTwoFactorDeviceKey> encryptedTwoFactorDeviceKeys;

    @Before
    public void setup() {
        encryptedTwoFactorDeviceKeys =  encryptedTwoFactorKeys.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey(),
                        entry -> entry.getValue().toEncryptedTwoFactorDeviceKey()));
    }


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
                encryptedTwoFactorDeviceKeys,
                new ArrayList<>()
        );

        EncryptedTwoFactorKey encryptedTwoFactorKey2 = new EncryptedTwoFactorKey(
                UUID.randomUUID().toString(),
                email,
                encryptedTwoFactorDeviceKeys,
                new ArrayList<>()
        );

        EncryptedTwoFactorKey encryptedTwoFactorKey3 = new EncryptedTwoFactorKey(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                encryptedTwoFactorDeviceKeys,
                new ArrayList<>()
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
                encryptedTwoFactorDeviceKeys,
                new ArrayList<>()
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

        Map<String, EncryptedTwoFactorDeviceKeyDTO> collect = getDeviceKeysAsDTO(encryptedTwoFactorKey);
        assertThat(body.getEncryptedTwoFactorKeys()).isEqualTo(collect);
    }

    private Map<String, EncryptedTwoFactorDeviceKeyDTO> getDeviceKeysAsDTO(EncryptedTwoFactorKey encryptedTwoFactorKey) {
        return encryptedTwoFactorKey.getEncryptedTwoFactorKeys()
                    .entrySet().stream()
                    .collect(Collectors.toMap(entry -> entry.getKey(),
                            entry -> EncryptedTwoFactorDeviceKeyDTO.from(entry.getValue())));
    }

    @Test
    public void update() {
        final String userId = UUID.randomUUID().toString();
        final Element updateKey = getGPP().getPairing().getG1().newRandomElement();
        final String enodedUpdateKey = ElementConverter.convert(updateKey);

        EncryptedTwoFactorKey encryptedTwoFactorKey = new EncryptedTwoFactorKey(
                userId,
                email,
                encryptedTwoFactorDeviceKeys,
                new ArrayList<>()
        );

        twoFactorKeyDB.insert(encryptedTwoFactorKey);

        TwoFactorUpdateKeyRequest twoFactorUpdateKeyRequest = new TwoFactorUpdateKeyRequest(
                enodedUpdateKey
        );

        ResponseEntity<TwoFactorKeyResponse> exchange = mutualAuthRestTemplate
                .exchange("/two-factor-keys/" + encryptedTwoFactorKey.getId(),
                        HttpMethod.PUT,
                        new HttpEntity<>(twoFactorUpdateKeyRequest),
                        TwoFactorKeyResponse.class);

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        TwoFactorKeyResponse body = exchange.getBody();
        assertThat(body.getId()).isEqualTo(encryptedTwoFactorKey.getId());
        assertThat(body.getUserId()).isEqualTo(encryptedTwoFactorKey.getUserId());
        assertThat(body.getOwnerId()).isEqualTo(encryptedTwoFactorKey.getDataOwnerId());
        Map<String, EncryptedTwoFactorDeviceKeyDTO> collect = getDeviceKeysAsDTO(encryptedTwoFactorKey);
        assertThat(body.getEncryptedTwoFactorKeys()).isEqualTo(collect);
        assertThat(body.getUpdates()).hasSize(1).containsOnly(enodedUpdateKey);
    }

    @Test
    public void remove() {
        EncryptedTwoFactorKey encryptedTwoFactorKey = new EncryptedTwoFactorKey(
                UUID.randomUUID().toString(),
                email,
                encryptedTwoFactorDeviceKeys,
                new ArrayList<>()
        );

        twoFactorKeyDB.insert(encryptedTwoFactorKey);

        ResponseEntity<TwoFactorKeyResponse> exchange = mutualAuthRestTemplate
                .exchange("/two-factor-keys/" + encryptedTwoFactorKey.getId(), HttpMethod.DELETE, HttpEntity.EMPTY,
                        TwoFactorKeyResponse.class);

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.NO_CONTENT);
        assertThat(twoFactorKeyDB.exist(encryptedTwoFactorKey.getId())).isFalse();
    }
}
