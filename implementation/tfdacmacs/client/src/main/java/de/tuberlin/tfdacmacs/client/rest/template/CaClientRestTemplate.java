package de.tuberlin.tfdacmacs.client.rest.template;

import de.tuberlin.tfdacmacs.client.register.data.dto.CertificateRequest;
import de.tuberlin.tfdacmacs.client.register.data.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.client.rest.CaClient;
import de.tuberlin.tfdacmacs.client.rest.error.InterServiceCallError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaClientRestTemplate implements CaClient {

    private final RestTemplate restTemplate;

    private <T> T request(String url, HttpMethod httpMethod, Class<T> responseType, Object body) {
        log.info("Asking CA for [{}:{}]", httpMethod, url);

        ResponseEntity<T> response = restTemplate.exchange(
                url,
                httpMethod,
                body != null ? new HttpEntity<>(body) : HttpEntity.EMPTY,
                responseType
        );

        postProcessResponse(response, url, httpMethod);
        log.info("Asking CA for [{}:{}]: {}", httpMethod, url, response.getStatusCode());
        return response.getBody();
    }

    private void postProcessResponse(ResponseEntity<?> response, String url, HttpMethod httpMethod) {

        if (response.getStatusCodeValue() < 200 || response.getStatusCodeValue() >= 300) {
            throw new InterServiceCallError(httpMethod, url, response.getStatusCode());
        }
    }

    public CertificateResponse certificateRequest(CertificateRequest certificateRequest) {
        return request("/certificates", HttpMethod.POST, CertificateResponse.class, certificateRequest);
    }
}
