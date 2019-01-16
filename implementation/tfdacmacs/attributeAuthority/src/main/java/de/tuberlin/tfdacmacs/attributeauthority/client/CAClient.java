package de.tuberlin.tfdacmacs.attributeauthority.client;

import de.tuberlin.tfdacmacs.lib.attributes.data.dto.AttributeCreationRequest;
import de.tuberlin.tfdacmacs.lib.attributes.data.dto.AttributeValueCreationRequest;
import de.tuberlin.tfdacmacs.lib.attributes.data.dto.AttributeValueUpdateRequest;
import de.tuberlin.tfdacmacs.lib.attributes.data.dto.PublicAttributeResponse;
import de.tuberlin.tfdacmacs.lib.authority.AttributeAuthorityPublicKeyRequest;
import de.tuberlin.tfdacmacs.lib.authority.AttributeAuthorityResponse;
import de.tuberlin.tfdacmacs.lib.certificate.data.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.lib.gpp.data.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.lib.user.data.dto.DeviceResponse;
import de.tuberlin.tfdacmacs.lib.user.data.dto.DeviceUpdateRequest;
import de.tuberlin.tfdacmacs.lib.user.data.dto.UserCreationRequest;
import de.tuberlin.tfdacmacs.lib.user.data.dto.UserResponse;

public interface CAClient {

    // GPP
    GlobalPublicParameterDTO getGPP();

    // USERS
    UserResponse createUser(UserCreationRequest userCreationRequest);
    UserResponse getUser(String id);
    DeviceResponse updateDevice(String userId, String deviceId, DeviceUpdateRequest deviceUpdateRequest);

    // CERTIFICATES
    CertificateResponse getCentralAuthorityCertificate();
    CertificateResponse getCertificate(String id);

    // AUTHORITY
    AttributeAuthorityResponse updateAuthorityPublicKey(String authorityId, AttributeAuthorityPublicKeyRequest attributeAuthorityPublicKeyRequest);

    // ATTRIBUTES
    PublicAttributeResponse createAttribute(AttributeCreationRequest attributeCreationRequest);
    PublicAttributeResponse createAttributeValue(String attributeId, AttributeValueCreationRequest attributeValueCreationRequest);
    PublicAttributeResponse updateAttributeValue(String attributeId, String attributeValueId, AttributeValueUpdateRequest attributeUpdateRequest);
}
