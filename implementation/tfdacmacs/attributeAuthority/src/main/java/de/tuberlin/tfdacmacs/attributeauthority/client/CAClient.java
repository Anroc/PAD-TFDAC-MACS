package de.tuberlin.tfdacmacs.attributeauthority.client;

import de.tuberlin.tfdacmacs.lib.certificate.data.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.lib.gpp.data.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.lib.user.data.dto.UserCreationRequest;
import de.tuberlin.tfdacmacs.lib.user.data.dto.UserResponse;

public interface CAClient {

    GlobalPublicParameterDTO getGPP();

    UserResponse createUser(UserCreationRequest userCreationRequest);

    CertificateResponse getCentralAuthorityCertificate();

}
