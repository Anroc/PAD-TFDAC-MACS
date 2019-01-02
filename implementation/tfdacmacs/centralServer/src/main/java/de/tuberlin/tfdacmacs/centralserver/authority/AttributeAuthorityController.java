package de.tuberlin.tfdacmacs.centralserver.authority;

import de.tuberlin.tfdacmacs.basics.authority.AttributeAuthorityResponse;
import de.tuberlin.tfdacmacs.basics.certificate.data.dto.CertificateRequest;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;
import de.tuberlin.tfdacmacs.basics.exceptions.BadRequestException;
import de.tuberlin.tfdacmacs.centralserver.certificate.CertificateService;
import de.tuberlin.tfdacmacs.centralserver.certificate.data.Certificate;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/authorities")
public class AttributeAuthorityController {

    private final CertificateService certificateService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public AttributeAuthorityResponse registerAttributeAuthority(@RequestBody @Valid CertificateRequest certificateRequest) {
        try {
            PKCS10CertificationRequest pkcs10CertificationRequest = new PKCS10CertificationRequest(
                    KeyConverter.from(certificateRequest.getCertificateRequest()).toByes());

            Certificate certificate = certificateService.certificateRequestAuthority(pkcs10CertificationRequest);

            return new AttributeAuthorityResponse(certificate.getCommonName(), certificate.getId());
        } catch (IOException e) {
            throw new BadRequestException("Certificate request was not a valid PKCS10CertificationRequest!", e);
        }
    }
}
