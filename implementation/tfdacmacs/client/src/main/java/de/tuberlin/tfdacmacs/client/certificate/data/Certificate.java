package de.tuberlin.tfdacmacs.client.certificate.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.cert.X509Certificate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Certificate {

    private String id;
    private String email;
    private X509Certificate certificate;
}
