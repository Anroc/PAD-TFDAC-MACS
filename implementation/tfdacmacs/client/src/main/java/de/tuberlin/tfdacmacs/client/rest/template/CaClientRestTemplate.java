package de.tuberlin.tfdacmacs.client.rest.template;

import de.tuberlin.tfdacmacs.client.attribute.data.dto.DeviceResponse;
import de.tuberlin.tfdacmacs.client.attribute.data.dto.PublicAttributeValueResponse;
import de.tuberlin.tfdacmacs.client.authority.data.dto.AttributeAuthorityResponse;
import de.tuberlin.tfdacmacs.client.gpp.data.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.client.register.data.dto.CertificateRequest;
import de.tuberlin.tfdacmacs.client.register.data.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.client.rest.CaClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class CaClientRestTemplate extends ClientRestTemplate implements CaClient {

    @Autowired
    public CaClientRestTemplate(@Qualifier(RestTemplateFactory.CA_REST_TEMPLATE_BEAN_NAME) RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
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
}
