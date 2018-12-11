package de.tuberlin.tfdacmacs.centralserver.key;

import de.tuberlin.tfdacmacs.IntegrationTestSuite;
import de.tuberlin.tfdacmacs.basics.crypto.rsa.converter.KeyConverter;
import de.tuberlin.tfdacmacs.basics.gpp.data.dto.RSAPublicKeyDTO;
import de.tuberlin.tfdacmacs.centralserver.key.data.RsaKeyPair;
import org.junit.Test;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class PublicKeyIntegrationTest extends IntegrationTestSuite  {

    @Test
    public void getPublicKey() {
        RSAPublicKeyDTO publicKeyResponse = restTemplate.getForObject("/keys", RSAPublicKeyDTO.class);
        assertThat(publicKeyResponse.getPublicKey()).isNotBlank();
        PublicKey publicKey = KeyConverter.from(publicKeyResponse.getPublicKey()).toPublicKey();
        assertThat(publicKey).isInstanceOf(RSAPublicKey.class);
        assertThat(keyDB.findEntity(RsaKeyPair.ID).get().getPublicKey()).isEqualTo(publicKey);
    }
}
