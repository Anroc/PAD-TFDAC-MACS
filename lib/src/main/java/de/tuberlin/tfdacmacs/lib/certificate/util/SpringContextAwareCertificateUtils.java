package de.tuberlin.tfdacmacs.lib.certificate.util;

import de.tuberlin.tfdacmacs.crypto.rsa.certificate.CertificateUtils;
import de.tuberlin.tfdacmacs.lib.config.KeyStoreConfig;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

@Component
@RequiredArgsConstructor
public class SpringContextAwareCertificateUtils {

    private final CertificateUtils certificateUtils = new CertificateUtils();
    private final KeyStoreConfig keyStoreConfig;

    public void validateCertificate(@NonNull X509Certificate x509Certificate, @NonNull X509Certificate... x509Certificates) {
        try {
            certificateUtils.validateCertificate(keyStoreConfig.getTrustStore(), keyStoreConfig.getTrustStorePassword(),
                    x509Certificate, x509Certificates);
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String extractCommonName(@NonNull X509Certificate x509Certificate) {
        return certificateUtils.extractCommonName(x509Certificate);
    }

    public String fingerprint(@NonNull PublicKey publicKey) {
        return certificateUtils.fingerprint(publicKey);
    }

    public String fingerprint(@NonNull X509Certificate x509Certificate) {
        return certificateUtils.fingerprint(x509Certificate);
    }
}
