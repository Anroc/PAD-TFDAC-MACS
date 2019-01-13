package de.tuberlin.tfdacmacs.client.certificate.data;

import lombok.Data;

import java.security.cert.X509Certificate;

@Data
public class Certificate {

    private final String id;
    private final String email;
    private final X509Certificate certificate;
}
