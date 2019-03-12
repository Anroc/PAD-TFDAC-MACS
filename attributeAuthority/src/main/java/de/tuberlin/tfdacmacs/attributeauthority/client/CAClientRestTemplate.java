package de.tuberlin.tfdacmacs.attributeauthority.client;

import de.tuberlin.tfdacmacs.attributeauthority.client.error.InterServiceCallError;
import de.tuberlin.tfdacmacs.attributeauthority.config.AttributeAuthorityConfig;
import de.tuberlin.tfdacmacs.lib.attributes.data.dto.AttributeCreationRequest;
import de.tuberlin.tfdacmacs.lib.attributes.data.dto.AttributeValueCreationRequest;
import de.tuberlin.tfdacmacs.lib.attributes.data.dto.PublicAttributeResponse;
import de.tuberlin.tfdacmacs.lib.attributes.data.dto.PublicAttributeValueResponse;
import de.tuberlin.tfdacmacs.lib.authority.AttributeAuthorityPublicKeyRequest;
import de.tuberlin.tfdacmacs.lib.authority.AttributeAuthorityResponse;
import de.tuberlin.tfdacmacs.lib.certificate.data.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.lib.ciphertext.data.dto.AttributeCipherTextUpdateRequest;
import de.tuberlin.tfdacmacs.lib.ciphertext.data.dto.CipherTextDTO;
import de.tuberlin.tfdacmacs.lib.config.KeyStoreConfig;
import de.tuberlin.tfdacmacs.lib.gpp.data.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.lib.user.data.dto.*;
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
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.util.List;

@Slf4j
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

    private <T> List<T> listRequest(String url, HttpMethod httpMethod, ParameterizedTypeReference<List<T>> responseType, Object body) {
        log.info("Asking CA for [{}:{}]", httpMethod, url);

        ResponseEntity<List<T>> response = restTemplate.exchange(
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

    @Override
    public GlobalPublicParameterDTO getGPP() {
        return request("/gpp", HttpMethod.GET, GlobalPublicParameterDTO.class, null);
    }

    @Override
    public UserResponse createUser(UserCreationRequest userCreationRequest) {
        return request("/users", HttpMethod.POST, UserResponse.class, userCreationRequest);
    }

    @Override
    public UserResponse getUser(String id) {
        return request(String.format("/users/%s", id), HttpMethod.GET, UserResponse.class, null);
    }

    @Override
    public UserResponse updateAttributeValueUpdateKey(String userId, AttributeValueUpdateKeyDTO attributeValueUpdateKeyDTO) {
        return request(String.format("/users/%s/attribute-update-key", userId), HttpMethod.PUT, UserResponse.class, attributeValueUpdateKeyDTO);
    }

    @Override
    public DeviceResponse updateDevice(String userId, String deviceId, DeviceUpdateRequest deviceUpdateRequest) {
        return request(String.format("/users/%s/devices/%s", userId, deviceId), HttpMethod.PUT, DeviceResponse.class, deviceUpdateRequest);
    }

    @Override
    public AttributeAuthorityResponse updateAuthorityPublicKey(String authorityId, AttributeAuthorityPublicKeyRequest attributeAuthorityPublicKeyRequest) {
        return request(String.format("/authorities/%s/public-key", authorityId), HttpMethod.PUT, AttributeAuthorityResponse.class, attributeAuthorityPublicKeyRequest);
    }

    @Override
    public CertificateResponse getCentralAuthorityCertificate() {
        return getCertificate("root");
    }

    @Override
    public CertificateResponse getCertificate(String id) {
        return request(String.format("/certificates/%s", id), HttpMethod.GET, CertificateResponse.class, null);
    }

    @Override
    public PublicAttributeResponse createAttribute(AttributeCreationRequest attributeCreationRequest) {
        return request("/attributes", HttpMethod.POST, PublicAttributeResponse.class, attributeCreationRequest);
    }

    @Override
    public PublicAttributeValueResponse createAttributeValue(String attributeId, AttributeValueCreationRequest attributeValueCreationRequest) {
        return request(String.format("/attributes/%s/values", attributeId), HttpMethod.POST, PublicAttributeValueResponse.class, attributeValueCreationRequest);
    }

    @Override
    public List<CipherTextDTO> getCipherTexts(List<String> attributeValueId) {
        String joinedQuery = StringUtils.collectionToDelimitedString(attributeValueId, ",");
        return listRequest(
                String.format("/ciphertexts?attrIds=%s&completeMatch=False", joinedQuery),
                HttpMethod.GET,
                new ParameterizedTypeReference<List<CipherTextDTO>>(){},
                null
        );
    }

    @Override
    public List<CipherTextDTO> updateCipherTexts(AttributeCipherTextUpdateRequest attributeCipherTextUpdateRequest) {
        return listRequest(
                String.format("/ciphertexts/update/attribute"),
                HttpMethod.PUT,
                new ParameterizedTypeReference<List<CipherTextDTO>>(){},
                attributeCipherTextUpdateRequest
        );    }
}
