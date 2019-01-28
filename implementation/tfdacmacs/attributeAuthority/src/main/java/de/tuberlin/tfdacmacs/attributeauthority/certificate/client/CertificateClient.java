package de.tuberlin.tfdacmacs.attributeauthority.certificate.client;

import de.tuberlin.tfdacmacs.attributeauthority.certificate.RootCertificateProvider;
import de.tuberlin.tfdacmacs.attributeauthority.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.attributeauthority.certificate.exceptions.CertificateUntrustedException;
import de.tuberlin.tfdacmacs.attributeauthority.client.CAClient;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;
import de.tuberlin.tfdacmacs.lib.certificate.data.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.lib.certificate.util.SpringContextAwareCertificateUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.cert.X509Certificate;

@Component
@Slf4j
@RequiredArgsConstructor
public class CertificateClient {

    private final CAClient caClient;
    private final SpringContextAwareCertificateUtils certificateUtils;
    private final RootCertificateProvider rootCertificateProvider;

    public Certificate getRootCertificate() {
        CertificateResponse centralAuthorityCertificate = caClient.getCentralAuthorityCertificate();
        Certificate certificate = mapToCertificate(centralAuthorityCertificate);
        validateCertificateChain(certificate.getCertificate());
        return certificate;
    }

    private Certificate mapToCertificate(@NonNull CertificateResponse certificateResponse) {
        return new Certificate(
                    certificateResponse.getId(),
                    KeyConverter.from(certificateResponse.getCertificate()).toX509Certificate()
            );
    }

    public Certificate getCertificate(@NonNull String id, @NonNull String userId) {
        CertificateResponse certificateResponse = caClient.getCertificate(id);
        Certificate certificate = mapToCertificate(certificateResponse);
        validateFingerprint(id, certificate.getCertificate());
        validateCommonName(userId, certificate.getCertificate());
        validateCertificateChain(certificate.getCertificate(), rootCertificateProvider.getRootCertificate().getCertificate());
        return certificate;
    }

    private void validateFingerprint(String id, X509Certificate certificate) {
        String calucatedFingerprint = certificateUtils.fingerprint(certificate);
        if( ! id.equals(calucatedFingerprint)) {
            throw new CertificateUntrustedException(
                    String.format("Fingerprint does not match. Expected %s but was %s.", id, calucatedFingerprint)
            );
        }
    }

    private void validateCommonName(@NonNull String id, @NonNull X509Certificate certificate) {
        String commonName = certificateUtils.extractCommonName(certificate);
        if(! commonName.equals(id)) {
            log.error("Certificate Untrusted:\n{}", certificate.toString());
            throw new CertificateUntrustedException(
                    String.format("CommonName [%s] does not match exactly the user name in DB [%s].", commonName, id)
            );
        }
    }

    private void validateCertificateChain(X509Certificate certificate, X509Certificate... chain) {
        certificateUtils.validateCertificate(certificate, chain);
    }
}
