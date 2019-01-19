package de.tuberlin.tfdacmacs.client.rest.template;

import de.tuberlin.tfdacmacs.client.rest.error.InterServiceCallError;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public abstract class ClientRestTemplate {

    private final RestTemplate restTemplate;
    private volatile Map<Long, HttpHeaders> httpHeaders = new HashMap<>();


    public <T> T request(String url, HttpMethod httpMethod, Class<T> responseType, Object body) {
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

        httpHeaders.remove(Thread.currentThread().getId());
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
            httpHeaders.remove(Thread.currentThread().getId());
            throw new InterServiceCallError(httpMethod, url, e.getStatusCode(), e);
        }
    }

    public ClientRestTemplate withHeaders(@NonNull HttpHeaders headers) {
        httpHeaders.put(Thread.currentThread().getId(), headers);
        return this;
    }
}
