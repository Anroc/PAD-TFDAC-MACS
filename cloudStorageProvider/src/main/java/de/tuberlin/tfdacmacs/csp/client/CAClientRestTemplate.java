package de.tuberlin.tfdacmacs.csp.client;

import de.tuberlin.tfdacmacs.csp.client.error.InterServiceCallError;
import de.tuberlin.tfdacmacs.csp.config.CSPConfig;
import de.tuberlin.tfdacmacs.lib.config.KeyStoreConfig;
import de.tuberlin.tfdacmacs.lib.gpp.data.dto.GlobalPublicParameterDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CAClientRestTemplate implements CAClient {

    private RestTemplate restTemplate;

    private final CSPConfig cspConfig;
    private final KeyStoreConfig keyStoreConfig;

    @PostConstruct
    public void initRestTemplate() {
        try {
            SSLContext sslContext = SSLContexts
                    .custom()
                    .loadTrustMaterial(ResourceUtils.getFile(keyStoreConfig.getTrustStore()), keyStoreConfig.getTrustStorePassword().toCharArray())
                    .loadKeyMaterial(
                            ResourceUtils.getFile(keyStoreConfig.getKeyStore()),
                            keyStoreConfig.getKeyStorePassword().toCharArray(),
                            keyStoreConfig.getKeyPassword().toCharArray()
                    ).build();
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
            HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();
            restTemplate = new RestTemplateBuilder().rootUri(cspConfig.getCaRootUrl()).build();
            ((HttpComponentsClientHttpRequestFactory) restTemplate.getRequestFactory()).setHttpClient(httpClient);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

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

    private <T> List<T> listRequest(String url, HttpMethod httpMethod, Class<T> responseType, Object body) {
        log.info("Asking CA for [{}:{}]", httpMethod, url);

        ResponseEntity<List<T>> response = restTemplate.exchange(
                url,
                httpMethod,
                body != null ? new HttpEntity<>(body) : HttpEntity.EMPTY,
                new ParameterizedTypeReference<List<T>>(){}
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

    @Override
    public GlobalPublicParameterDTO getGPP() {
        return request("/gpp", HttpMethod.GET, GlobalPublicParameterDTO.class, null);
    }
}
