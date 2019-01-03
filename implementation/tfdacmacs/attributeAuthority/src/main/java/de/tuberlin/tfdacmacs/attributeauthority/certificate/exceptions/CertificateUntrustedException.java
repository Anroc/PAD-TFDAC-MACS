package de.tuberlin.tfdacmacs.attributeauthority.certificate.exceptions;

import java.security.cert.X509Certificate;

public class CertificateUntrustedException extends RuntimeException {
    public CertificateUntrustedException(String message) {
        super(message);
    }
}
