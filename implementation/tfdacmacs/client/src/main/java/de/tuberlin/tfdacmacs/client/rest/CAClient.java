package de.tuberlin.tfdacmacs.client.rest;

import de.tuberlin.tfdacmacs.client.attribute.client.dto.DeviceResponse;
import de.tuberlin.tfdacmacs.client.attribute.client.dto.PublicAttributeValueResponse;
import de.tuberlin.tfdacmacs.client.authority.client.dto.AttributeAuthorityResponse;
import de.tuberlin.tfdacmacs.client.certificate.client.dto.CertificateRequest;
import de.tuberlin.tfdacmacs.client.certificate.client.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.client.csp.client.dto.CipherTextDTO;
import de.tuberlin.tfdacmacs.client.gpp.client.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.client.twofactor.client.dto.TwoFactorKeyRequest;
import de.tuberlin.tfdacmacs.client.twofactor.client.dto.TwoFactorKeyResponse;
import de.tuberlin.tfdacmacs.client.twofactor.client.dto.UserResponse;

import java.util.List;

public interface CAClient {

    GlobalPublicParameterDTO getGPP();

    CertificateResponse postCertificateRequest(CertificateRequest certificateRequest);
    CertificateResponse getCertificate(String certificateId);

    UserResponse getUser(String userId);

    AttributeAuthorityResponse getAuthority(String authorityId);

    DeviceResponse getAttributes(String userId, String deviceId);
    PublicAttributeValueResponse getAttributeValue(String attributeId, String valueId);

    void createCipherText(CipherTextDTO cipherTextDTO);
    List<CipherTextDTO> getCipherTexts(List<String> attributeIds);

    TwoFactorKeyResponse createTwoFactorKey(TwoFactorKeyRequest twoFactorKeyRequest);
    List<TwoFactorKeyResponse> getTwoFactorKeys(String email);

}
