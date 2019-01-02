package de.tuberlin.tfdacmacs.attributeauthority.client;

import de.tuberlin.tfdacmacs.attributeauthority.client.error.InterServiceCallError;
import de.tuberlin.tfdacmacs.attributeauthority.config.AttributeAuthorityConfig;
import de.tuberlin.tfdacmacs.basics.certificate.data.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.basics.config.KeyStoreConfig;
import de.tuberlin.tfdacmacs.basics.gpp.data.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.basics.user.data.dto.UserCreationRequest;
import de.tuberlin.tfdacmacs.basics.user.data.dto.UserResponse;
import lombok.RequiredArgsConstructor;
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

@Component
@RequiredArgsConstructor
public class CAClientRestTemplate implements CAClient {

    private RestTemplate restTemplate;

    private final AttributeAuthorityConfig attributeAuthorityConfig;
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
            restTemplate = new RestTemplateBuilder().rootUri(attributeAuthorityConfig.getCaRootUrl()).build();
            ((HttpComponentsClientHttpRequestFactory) restTemplate.getRequestFactory()).setHttpClient(httpClient);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T request(String url, HttpMethod httpMethod, Class<T> responseType, Object body) {
        ResponseEntity<T> response = restTemplate.exchange(
                url,
                httpMethod,
                body != null ? new HttpEntity<>(body) : HttpEntity.EMPTY,
                responseType
        );

        postProcessResponse(response, url, httpMethod);
        return response.getBody();
    }

    private <T> List<T> listRequest(String url, HttpMethod httpMethod, Class<T> responseType, Object body) {
        ResponseEntity<List<T>> response = restTemplate.exchange(
                url,
                httpMethod,
                body != null ? new HttpEntity<>(body) : HttpEntity.EMPTY,
                new ParameterizedTypeReference<List<T>>(){}
        );

        postProcessResponse(response, url, httpMethod);
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

    @Override
    public UserResponse createUser(UserCreationRequest userCreationRequest) {
        return request("/users", HttpMethod.POST, UserResponse.class, userCreationRequest);
    }

    @Override
    public CertificateResponse getCentralAuthorityCertificate() {
        return request("/certificates/root", HttpMethod.GET, CertificateResponse.class, null);
    }
}
