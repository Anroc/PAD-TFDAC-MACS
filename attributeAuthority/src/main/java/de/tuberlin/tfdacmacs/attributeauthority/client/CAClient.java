package de.tuberlin.tfdacmacs.attributeauthority.client;

import de.tuberlin.tfdacmacs.lib.authority.AttributeAuthorityPublicKeyRequest;
import de.tuberlin.tfdacmacs.lib.authority.AttributeAuthorityResponse;
import de.tuberlin.tfdacmacs.lib.certificate.data.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.lib.gpp.data.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.lib.user.data.dto.DeviceResponse;
import de.tuberlin.tfdacmacs.lib.user.data.dto.DeviceUpdateRequest;
import de.tuberlin.tfdacmacs.lib.user.data.dto.UserCreationRequest;
import de.tuberlin.tfdacmacs.lib.user.data.dto.UserResponse;

public interface CAClient {

    GlobalPublicParameterDTO getGPP();

    UserResponse createUser(UserCreationRequest userCreationRequest);

    CertificateResponse getCentralAuthorityCertificate();

    CertificateResponse getCertificate(String id);

    UserResponse getUser(String id);

    DeviceResponse updateDevice(String userId, String deviceId, DeviceUpdateRequest deviceUpdateRequest);

    AttributeAuthorityResponse updateAuthorityPublicKey(String authorityId, AttributeAuthorityPublicKeyRequest attributeAuthorityPublicKeyRequest);
}
