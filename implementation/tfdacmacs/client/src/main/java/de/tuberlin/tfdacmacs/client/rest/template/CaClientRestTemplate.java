package de.tuberlin.tfdacmacs.client.rest.template;

import de.tuberlin.tfdacmacs.client.attribute.data.dto.DeviceResponse;
import de.tuberlin.tfdacmacs.client.gpp.data.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.client.register.data.dto.CertificateRequest;
import de.tuberlin.tfdacmacs.client.register.data.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.client.rest.CaClient;
import de.tuberlin.tfdacmacs.client.rest.error.InterServiceCallError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaClientRestTemplate implements CaClient {

    private final RestTemplate restTemplate;

    private <T> T request(String url, HttpMethod httpMethod, Class<T> responseType, Object body) {
        ResponseEntity<T> response = null;
        do {
            if(response != null) {
                log.info("Waiting 10s until the next retry...");
            }

            log.info("Asking CA for [{}:{}]", httpMethod, url);

            response = restTemplate.exchange(
                    url,
                    httpMethod,
                    body != null ? new HttpEntity<>(body) : HttpEntity.EMPTY,
                    responseType
            );

            log.info("Asking CA for [{}:{}]: {}", httpMethod, url, response.getStatusCode());
        } while (shouldRetry(response));

        postProcessResponse(response, url, httpMethod);
        return response.getBody();
    }

    private boolean shouldRetry(ResponseEntity<?> response) {
        return response.getStatusCode() == HttpStatus.PRECONDITION_FAILED;
    }

    private void postProcessResponse(ResponseEntity<?> response, String url, HttpMethod httpMethod) {
        if (response.getStatusCodeValue() < 200 || response.getStatusCodeValue() >= 300) {
            throw new InterServiceCallError(httpMethod, url, response.getStatusCode());
        }
    }

    public CertificateResponse postCertificateRequest(CertificateRequest certificateRequest) {
        return request("/certificates", HttpMethod.POST, CertificateResponse.class, certificateRequest);
    }

    @Override
    public DeviceResponse getAttributes(String userId, String deviceId) {
        return request(
                String.format("/users/%s/devices/%s", userId, deviceId),
                HttpMethod.GET,
                DeviceResponse.class,
                null
        );
    }

    @Override
    public GlobalPublicParameterDTO getGPP() {
        return request(
                "/gpp",
                HttpMethod.GET,
                GlobalPublicParameterDTO.class,
                null
        );
    }
}
