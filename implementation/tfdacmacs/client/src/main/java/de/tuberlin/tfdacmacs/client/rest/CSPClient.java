package de.tuberlin.tfdacmacs.client.rest;

import de.tuberlin.tfdacmacs.client.csp.client.dto.FileInformationResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

public interface CSPClient {

    CSPClient withHeaders(HttpHeaders headers);

    void createFile(String id, MultiValueMap<String, Object> file);

    byte[] getFile(String fileId);

    FileInformationResponse getFileInformation(String fileId);
}
