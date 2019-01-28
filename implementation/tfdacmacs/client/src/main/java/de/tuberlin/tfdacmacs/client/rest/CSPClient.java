package de.tuberlin.tfdacmacs.client.rest;

import de.tuberlin.tfdacmacs.client.csp.data.dto.CipherTextDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

import java.util.List;

public interface CSPClient {

    void createCipherText(CipherTextDTO cipherTextDTO);

    CSPClient withHeaders(HttpHeaders headers);

    void createFile(String id, MultiValueMap<String, Object> file);

    List<CipherTextDTO> getCipherTexts(List<String> attributeIds);
}
