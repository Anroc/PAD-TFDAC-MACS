package de.tuberlin.tfdacmacs.client.authority.exception;

public class CertificateManipulationException extends RuntimeException {

    public CertificateManipulationException(String expectedCertId, String actualCertId) {
        super(String.format(
                "Certificate [%s] rejected: "
                        + "Certificate fingerprint does not match. "
                        + "It might have changed or was tampered. [%s!=%s]", actualCertId, expectedCertId, actualCertId
        ));
    }
}
