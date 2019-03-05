package de.tuberlin.tfdacmacs.attributeauthority.client;

import de.tuberlin.tfdacmacs.lib.attributes.data.dto.AttributeCreationRequest;
import de.tuberlin.tfdacmacs.lib.attributes.data.dto.AttributeValueCreationRequest;
import de.tuberlin.tfdacmacs.lib.attributes.data.dto.PublicAttributeResponse;
import de.tuberlin.tfdacmacs.lib.attributes.data.dto.PublicAttributeValueResponse;
import de.tuberlin.tfdacmacs.lib.authority.AttributeAuthorityPublicKeyRequest;
import de.tuberlin.tfdacmacs.lib.authority.AttributeAuthorityResponse;
import de.tuberlin.tfdacmacs.lib.certificate.data.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.lib.gpp.data.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.lib.user.data.dto.*;

public interface CAClient {

    // GPP
    GlobalPublicParameterDTO getGPP();

    // USERS
    UserResponse createUser(UserCreationRequest userCreationRequest);
    UserResponse getUser(String id);
    UserResponse updateAttributeValueUpdateKey(String userId, AttributeValueUpdateKeyDTO attributeValueUpdateKeyDTO);
    DeviceResponse updateDevice(String userId, String deviceId, DeviceUpdateRequest deviceUpdateRequest);

    // CERTIFICATES
    CertificateResponse getCentralAuthorityCertificate();
    CertificateResponse getCertificate(String id);

    // AUTHORITY
    AttributeAuthorityResponse updateAuthorityPublicKey(String authorityId, AttributeAuthorityPublicKeyRequest attributeAuthorityPublicKeyRequest);

    // ATTRIBUTES
    PublicAttributeResponse createAttribute(AttributeCreationRequest attributeCreationRequest);
    PublicAttributeValueResponse createAttributeValue(String attributeId, AttributeValueCreationRequest attributeValueCreationRequest);
    PublicAttributeValueResponse updateAttributeValue(String attributeId, String attributeValueId, AttributeValueCreationRequest attributeValueCreationRequest);
}
