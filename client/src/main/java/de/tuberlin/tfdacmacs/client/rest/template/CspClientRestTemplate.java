package de.tuberlin.tfdacmacs.client.rest.template;

import de.tuberlin.tfdacmacs.client.csp.data.dto.FileInformationResponse;
import de.tuberlin.tfdacmacs.client.rest.CSPClient;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class CspClientRestTemplate extends ClientRestTemplate implements CSPClient {

    @Override
    public CspClientRestTemplate withHeaders(@NonNull HttpHeaders headers) {
        return (CspClientRestTemplate) super.withHeaders(headers);
    }

    @Autowired
    public CspClientRestTemplate(@Qualifier(RestTemplateFactory.CSP_REST_TEMPLATE_BEAN_NAME) RestTemplate restTemplate) {
        super(restTemplate, "CSP");
    }

    @Override
    public void createFile(String id, MultiValueMap<String, Object> file) {
        request(String.format("/files?id=%s", id),
                HttpMethod.POST,
                Void.class,
                file
        );
    }

    @Override
    public byte[] getFile(String fileId) {
        return request(String.format("/files/%s", fileId),
                HttpMethod.GET,
                byte[].class,
                null
        );
    }

    @Override
    public FileInformationResponse getFileInformation(String fileId) {
        return request(String.format("/files/%s/information", fileId),
                HttpMethod.GET,
                FileInformationResponse.class,
                null
        );
    }
}
