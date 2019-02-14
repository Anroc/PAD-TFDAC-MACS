package de.tuberlin.tfdacmacs.client.rest.template;

import de.tuberlin.tfdacmacs.client.attribute.client.dto.DeviceResponse;
import de.tuberlin.tfdacmacs.client.attribute.client.dto.PublicAttributeValueResponse;
import de.tuberlin.tfdacmacs.client.authority.client.dto.AttributeAuthorityResponse;
import de.tuberlin.tfdacmacs.client.certificate.client.dto.CertificateRequest;
import de.tuberlin.tfdacmacs.client.certificate.client.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.client.csp.client.dto.CipherTextDTO;
import de.tuberlin.tfdacmacs.client.gpp.client.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.client.rest.CAClient;
import de.tuberlin.tfdacmacs.client.rest.factory.RestTemplateFactory;
import de.tuberlin.tfdacmacs.client.twofactor.client.dto.TwoFactorKeyRequest;
import de.tuberlin.tfdacmacs.client.twofactor.client.dto.TwoFactorKeyResponse;
import de.tuberlin.tfdacmacs.client.twofactor.client.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
public class CAClientRestTemplate extends ClientRestTemplate implements CAClient {

    @Autowired
    public CAClientRestTemplate(@Qualifier(RestTemplateFactory.CA_REST_TEMPLATE_BEAN_NAME) RestTemplate restTemplate) {
        super(restTemplate, "CA");
    }

    @Override
    public CertificateResponse postCertificateRequest(CertificateRequest certificateRequest) {
        return request("/certificates", HttpMethod.POST, CertificateResponse.class, certificateRequest);
    }

    @Override
    public CertificateResponse getCertificate(String certificateId) {
        return request(
                "/certificates/" + certificateId,
                HttpMethod.GET,
                CertificateResponse.class,
                null
        );
    }

    @Override
    public UserResponse getUser(String userId) {
        return request("/users/" + userId,
                HttpMethod.GET,
                UserResponse.class,
                null
        );
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

    @Override
    public PublicAttributeValueResponse getAttributeValue(String attributeId, String valueId) {
        return request(
                String.format("/attributes/%s/values/%s", attributeId, valueId),
                HttpMethod.GET,
                PublicAttributeValueResponse.class,
                null
        );
    }

    @Override
    public AttributeAuthorityResponse getAuthority(String authorityId) {
        return request(
                String.format("/authorities/%s", authorityId),
                HttpMethod.GET,
                AttributeAuthorityResponse.class,
                null
        );
    }

    @Override
    public void createCipherText(CipherTextDTO cipherTextDTO) {
        request("/ciphertexts",
                HttpMethod.POST,
                CipherTextDTO.class,
                cipherTextDTO
        );
    }

    @Override
    public List<CipherTextDTO> getCipherTexts(List<String> attributeIds) {
        String joinedQuery = StringUtils.collectionToDelimitedString(attributeIds, ",");
        return listRequest(
                String.format("/ciphertexts?attrIds=%s", joinedQuery),
                HttpMethod.GET,
                new ParameterizedTypeReference<List<CipherTextDTO>>(){},
                null
        );
    }

    @Override
    public List<CipherTextDTO> getCipherTexts(String ownerId) {
        return listRequest(
                String.format("/ciphertexts?ownerId=%s", ownerId),
                HttpMethod.GET,
                new ParameterizedTypeReference<List<CipherTextDTO>>(){},
                null
        );
    }

    @Override
    public TwoFactorKeyResponse createTwoFactorKey(TwoFactorKeyRequest twoFactorKeyRequest) {
        return request(
                "/two-factor-keys",
                HttpMethod.POST,
                TwoFactorKeyResponse.class,
                twoFactorKeyRequest
        );
    }

    @Override
    public List<TwoFactorKeyResponse> getTwoFactorKeys(String email) {
        return listRequest(
                "/two-factor-keys",
                HttpMethod.GET,
                new ParameterizedTypeReference<List<TwoFactorKeyResponse>>() {},
                null
        );
    }

}
