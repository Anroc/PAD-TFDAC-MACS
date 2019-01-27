package de.tuberlin.tfdacmacs.ciphertext;

import de.tuberlin.tfdacmacs.RestTestSuite;
import de.tuberlin.tfdacmacs.csp.ciphertext.data.CipherTextEntity;
import de.tuberlin.tfdacmacs.csp.ciphertext.data.dto.CipherTextDTO;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class CipherTextControllerRestTest extends RestTestSuite {

    @Test
    public void createCipherText() {
        CipherTextEntity originalCT = cipherTextTestFactory.createRandom();
        CipherTextDTO ctDTO = CipherTextDTO.from(originalCT);

        ResponseEntity<CipherTextDTO> exchange = mutualAuthRestTemplate
                .exchange("/ciphertexts", HttpMethod.POST, new HttpEntity<>(ctDTO), CipherTextDTO.class);

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.CREATED);

        String id = exchange.getBody().getId();
        CipherTextEntity cipherTextEntity = cipherTextDB.findEntity(id).get();
        assertThat(cipherTextEntity).isEqualTo(originalCT);
    }

    @Test
    public void getCipherText() {
        CipherTextEntity originalCT = cipherTextTestFactory.createRandom();
        cipherTextDB.insert(originalCT);

        ResponseEntity<CipherTextDTO> exchange = mutualAuthRestTemplate
                .exchange("/ciphertexts/" + originalCT.getId(), HttpMethod.GET, HttpEntity.EMPTY, CipherTextDTO.class);

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);

        CipherTextEntity ct = exchange.getBody().toCipherTextEntity(gppTestFactory.create().getPairing().getG1());
        assertThat(ct).isEqualTo(originalCT);
    }
}
