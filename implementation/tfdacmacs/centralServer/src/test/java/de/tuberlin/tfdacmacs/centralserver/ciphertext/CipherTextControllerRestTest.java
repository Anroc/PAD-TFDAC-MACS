package de.tuberlin.tfdacmacs.centralserver.ciphertext;

import de.tuberlin.tfdacmacs.RestTestSuite;
import de.tuberlin.tfdacmacs.centralserver.ciphertext.data.CipherTextEntity;
import de.tuberlin.tfdacmacs.lib.ciphertext.data.dto.CipherTextDTO;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CipherTextControllerRestTest extends RestTestSuite {

    @Test
    public void createCipherText() {
        CipherTextEntity originalCT = cipherTextTestFactory.createRandom();
        CipherTextDTO ctDTO = cipherTextController.buildResponse(originalCT);

        ResponseEntity<CipherTextDTO> exchange = mutualAuthRestTemplate
                .exchange("/ciphertexts", HttpMethod.POST, new HttpEntity<>(ctDTO), CipherTextDTO.class);

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.CREATED);

        String id = exchange.getBody().getId();
        CipherTextEntity cipherTextEntity = cipherTextDB.findEntity(id).get();
        assertThat(cipherTextController.buildResponse(cipherTextEntity)).isEqualTo(cipherTextController.buildResponse(originalCT));
    }

    @Test
    public void getCipherText() {
        CipherTextEntity originalCT = cipherTextTestFactory.createRandom();
        cipherTextDB.insert(originalCT);

        ResponseEntity<CipherTextDTO> exchange = mutualAuthRestTemplate
                .exchange("/ciphertexts/" + originalCT.getId(), HttpMethod.GET, HttpEntity.EMPTY, CipherTextDTO.class);

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);

        CipherTextEntity ct = cipherTextController.toCipherTextEntity(
                exchange.getBody(),
                getGPP().g1(),
                getGPP().gt());
        assertThat(ct).isEqualTo(originalCT);
    }

    @Test
    public void getCipherTexts() {
        String attributeId = "aa.tu-berlin.de.role:Student";
        CipherTextEntity cipherTextEntity1 = cipherTextTestFactory.create(attributeId, "aa.tu-berlin.de.role:Professor");
        CipherTextEntity cipherTextEntity2 = cipherTextTestFactory.create(attributeId);
        CipherTextEntity cipherTextEntity3 = cipherTextTestFactory.create("aa.tu-berlin.de.xx:xx");
        cipherTextDB.insert(cipherTextEntity1);
        cipherTextDB.insert(cipherTextEntity2);
        cipherTextDB.insert(cipherTextEntity3);

        ResponseEntity<List<CipherTextDTO>> exchange = mutualAuthRestTemplate
                .exchange(cipherTextsQueryParameter(attributeId),
                        HttpMethod.GET,
                        HttpEntity.EMPTY,
                        new ParameterizedTypeReference<List<CipherTextDTO>>() {});

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        List<CipherTextDTO> body = exchange.getBody();
        assertThat(body).hasSize(1);
        assertThat(body.stream().map(CipherTextDTO::getId)).containsExactly(cipherTextEntity2.getId());
    }

    @Test
    public void getCipherTexts_passes_on2Attributes() {
        String attributeId = "aa.tu-berlin.de.role:Student";
        String attributeId2 = "aa.tu-berlin.de.role:Professor";

        CipherTextEntity cipherTextEntity1 = cipherTextTestFactory.create(attributeId, attributeId2);
        CipherTextEntity cipherTextEntity2 = cipherTextTestFactory.create(attributeId, attributeId2, "aa.tu-berlin.de.xx:xx");
        cipherTextDB.insert(cipherTextEntity1);
        cipherTextDB.insert(cipherTextEntity2);

        ResponseEntity<List<CipherTextDTO>> exchange = mutualAuthRestTemplate
                .exchange(cipherTextsQueryParameter(attributeId, attributeId2),
                        HttpMethod.GET,
                        HttpEntity.EMPTY,
                        new ParameterizedTypeReference<List<CipherTextDTO>>() {});

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        List<CipherTextDTO> body = exchange.getBody();
        assertThat(body).hasSize(1);
        assertThat(body.stream().map(CipherTextDTO::getId)).containsExactly(
                cipherTextEntity1.getId());
    }

    private String cipherTextsQueryParameter(String... attributeIds) {
        return "/ciphertexts?attrIds=" + StringUtils.collectionToDelimitedString(Arrays.asList(attributeIds), ",");
    }
}
