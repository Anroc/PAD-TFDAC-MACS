package de.tuberlin.tfdacmacs.client.rest;

import de.tuberlin.tfdacmacs.client.attribute.data.dto.DeviceResponse;
import de.tuberlin.tfdacmacs.client.attribute.data.dto.PublicAttributeValueResponse;
import de.tuberlin.tfdacmacs.client.authority.data.dto.AttributeAuthorityResponse;
import de.tuberlin.tfdacmacs.client.csp.data.dto.CipherTextDTO;
import de.tuberlin.tfdacmacs.client.gpp.data.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.client.register.data.dto.CertificateRequest;
import de.tuberlin.tfdacmacs.client.register.data.dto.CertificateResponse;

import java.util.List;

public interface CAClient {

    GlobalPublicParameterDTO getGPP();

    CertificateResponse postCertificateRequest(CertificateRequest certificateRequest);
    CertificateResponse getCertificate(String certificateId);

    AttributeAuthorityResponse getAuthority(String authorityId);

    DeviceResponse getAttributes(String userId, String deviceId);
    PublicAttributeValueResponse getAttributeValue(String attributeId, String valueId);

    void createCipherText(CipherTextDTO cipherTextDTO);
    List<CipherTextDTO> getCipherTexts(List<String> attributeIds);
}
