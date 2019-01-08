package de.tuberlin.tfdacmacs.client.rest;

import de.tuberlin.tfdacmacs.client.register.data.dto.CertificateRequest;
import de.tuberlin.tfdacmacs.client.register.data.dto.CertificateResponse;

public interface CaClient {

    CertificateResponse certificateRequest(CertificateRequest certificateRequest);

}
