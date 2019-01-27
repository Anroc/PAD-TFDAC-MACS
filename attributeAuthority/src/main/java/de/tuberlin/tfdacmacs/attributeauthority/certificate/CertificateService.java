package de.tuberlin.tfdacmacs.attributeauthority.certificate;

import de.tuberlin.tfdacmacs.attributeauthority.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.crypto.rsa.certificate.CertificateUtils;
import de.tuberlin.tfdacmacs.crypto.rsa.certificate.JavaKeyStore;
import de.tuberlin.tfdacmacs.lib.config.KeyStoreConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.KeyStoreException;
import java.security.cert.X509Certificate;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final JavaKeyStore javaKeyStore;
    private final KeyStoreConfig keyStoreConfig;
    private final CertificateUtils certificateUtils;

    public Certificate getCertificate() {
        try {
            X509Certificate certificate = (X509Certificate) javaKeyStore.getCertificate(keyStoreConfig.getKeyAlias());
            return new Certificate(
                certificateUtils.fingerprint(certificate), certificate
            );
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }
}
