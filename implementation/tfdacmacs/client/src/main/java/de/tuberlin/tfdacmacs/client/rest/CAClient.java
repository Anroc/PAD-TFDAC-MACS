package de.tuberlin.tfdacmacs.client.rest;

import de.tuberlin.tfdacmacs.client.attribute.data.dto.DeviceResponse;
import de.tuberlin.tfdacmacs.client.attribute.data.dto.PublicAttributeValueResponse;
import de.tuberlin.tfdacmacs.client.authority.data.dto.AttributeAuthorityResponse;
import de.tuberlin.tfdacmacs.client.gpp.data.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.client.register.data.dto.CertificateRequest;
import de.tuberlin.tfdacmacs.client.register.data.dto.CertificateResponse;

public interface CAClient {

    CertificateResponse postCertificateRequest(CertificateRequest certificateRequest);

    CertificateResponse getCertificate(String certificateId);

    DeviceResponse getAttributes(String userId, String deviceId);

    GlobalPublicParameterDTO getGPP();

    PublicAttributeValueResponse getAttributeValue(String attributeId, String valueId);

    AttributeAuthorityResponse getAuthority(String authorityId);
}
