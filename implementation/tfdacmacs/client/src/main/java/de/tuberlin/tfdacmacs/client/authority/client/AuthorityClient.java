package de.tuberlin.tfdacmacs.client.authority.client;

import de.tuberlin.tfdacmacs.client.authority.data.TrustedAuthority;
import de.tuberlin.tfdacmacs.client.authority.client.dto.AttributeAuthorityResponse;
import de.tuberlin.tfdacmacs.client.authority.client.dto.AuthorityInformationResponse;
import de.tuberlin.tfdacmacs.client.authority.exception.CertificateManipulationException;
import de.tuberlin.tfdacmacs.client.gpp.GPPService;
import de.tuberlin.tfdacmacs.client.rest.AAClient;
import de.tuberlin.tfdacmacs.client.rest.CAClient;
import de.tuberlin.tfdacmacs.client.rest.SemanticValidator;
import de.tuberlin.tfdacmacs.client.rest.error.InterServiceCallError;
import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AuthorityKey;
import de.tuberlin.tfdacmacs.crypto.rsa.certificate.CertificateUtils;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AuthorityClient {

    private final CAClient caClient;
    private final AAClient aaClient;
    private final GPPService gppService;

    private final SemanticValidator semanticValidator;
    private final CertificateUtils certificateUtils;

    public Optional<AuthorityKey.Public> findAuthorityKey(@NonNull String authorityId) {
        try {
            AttributeAuthorityResponse authority = caClient.getAuthority(authorityId);
            semanticValidator.verifySignature(authority.getPublicKey(), authority.getSignature(), authorityId);

            return Optional.of(
                    new AuthorityKey.Public<>(
                        ElementConverter.convert(
                            authority.getPublicKey(),
                            gppService.getGPP().gt()),
                            authority.getVersion()));
        } catch(InterServiceCallError e) {
            if(e.getResponseStatus() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            } else {
                throw e;
            }
        }
    }

    public List<TrustedAuthority> retrieveTrustedAuthorities() {
        AuthorityInformationResponse authorityInformationResponse = aaClient.getTrustedAuthorities();
        List<TrustedAuthority> collect = authorityInformationResponse
                .getTrustedAuthorityIds()
                .entrySet()
                .stream()
                .map(entry -> new TrustedAuthority(
                        entry.getKey(),
                        entry.getValue(),
                        retrieveCertificate(entry.getValue())))
                .collect(Collectors.toList());
        // add own attribute authority
        collect.add(new TrustedAuthority(
                authorityInformationResponse.getId(),
                authorityInformationResponse.getCertificateId(),
                retrieveCertificate(authorityInformationResponse.getCertificateId())
        ));
        return collect;
    }

    public X509Certificate retrieveCertificate(String certificateId) {
        X509Certificate x509Certificate = KeyConverter.from(caClient.getCertificate(certificateId).getCertificate())
                .toX509Certificate();

        String fingerprint = certificateUtils.fingerprint(x509Certificate);
        if(! fingerprint.equals(certificateId)) {
            throw new CertificateManipulationException(certificateId, fingerprint);
        }

        return x509Certificate;
    }
}
