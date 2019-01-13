package de.tuberlin.tfdacmacs.client.rest;

import de.tuberlin.tfdacmacs.client.attribute.data.dto.DeviceResponse;
import de.tuberlin.tfdacmacs.client.gpp.data.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.client.register.data.dto.CertificateRequest;
import de.tuberlin.tfdacmacs.client.register.data.dto.CertificateResponse;

public interface CaClient {

    CertificateResponse postCertificateRequest(CertificateRequest certificateRequest);

    DeviceResponse getAttributes(String userId, String deviceId);

    GlobalPublicParameterDTO getGPP();

}
