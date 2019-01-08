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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaClientRestTemplate implements CaClient {

    private final RestTemplate restTemplate;

    private <T> T request(String url, HttpMethod httpMethod, Class<T> responseType, Object body) {
        HttpStatus responseStatus = null;
        ResponseEntity<T> response = null;

        do {
            if(responseStatus != null) {
                log.info("Waiting 10s until the next retry...");
                waitSeconds(10);
            }

            log.info("Asking CA for [{}:{}]", httpMethod, url);

            try {
                response = restTemplate.exchange(
                        url,
                        httpMethod,
                        body != null ? new HttpEntity<>(body) : HttpEntity.EMPTY,
                        responseType
                );
                responseStatus = response.getStatusCode();
            } catch(HttpClientErrorException e) {
                responseStatus = e.getStatusCode();
                handleClientErrorException(url, httpMethod, e);
            }

            log.info("Asking CA for [{}:{}]: {}", httpMethod, url, responseStatus);
        } while (shouldRetry(responseStatus));

        return response.getBody();
    }

    private void waitSeconds(int timeInSeconds) {
        try {
            Thread.sleep(timeInSeconds * 1000);
        } catch (InterruptedException e) {
            log.warn("Interrupted.", e);
        }
    }

    private boolean shouldRetry(HttpStatus httpStatus) {
        return httpStatus == HttpStatus.PRECONDITION_FAILED;
    }

    private void handleClientErrorException(String url, HttpMethod httpMethod, HttpClientErrorException e) {
        if(! shouldRetry(e.getStatusCode())) {
            throw new InterServiceCallError(httpMethod, url, e.getStatusCode(), e);
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
