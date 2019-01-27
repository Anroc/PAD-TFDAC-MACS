package de.tuberlin.tfdacmacs.attributeauthority.authority;

import de.tuberlin.tfdacmacs.attributeauthority.authority.data.TrustedAuthority;
import de.tuberlin.tfdacmacs.attributeauthority.authority.data.dto.AuthorityInformationResponse;
import de.tuberlin.tfdacmacs.attributeauthority.authority.data.dto.TrustedAuthorityCreationRequest;
import de.tuberlin.tfdacmacs.attributeauthority.certificate.CertificateService;
import de.tuberlin.tfdacmacs.attributeauthority.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.attributeauthority.config.AttributeAuthorityConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/authority")
@RequiredArgsConstructor
public class AttributeAuthorityController {

    private final CertificateService certificateService;
    private final AttributeAuthorityConfig config;
    private final TrustedAuthorityService trustedAuthorityService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public AuthorityInformationResponse getAuthorityInformation() {
        Certificate certificate = certificateService.getCertificate();
        return new AuthorityInformationResponse(
                config.getId(),
                certificate.getId(),
                trustedAuthorityService.findAll()
                    .collect(Collectors.toMap(TrustedAuthority::getId, TrustedAuthority::getCertificateId))
        );
    }

    @PostMapping("/trusted-authority")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorityInformationResponse addTrustedAuthority(
            @Valid @RequestBody TrustedAuthorityCreationRequest trustedAuthorityCreationRequest) {
        trustedAuthorityService.createTrustedAuthority(
                new TrustedAuthority(
                        trustedAuthorityCreationRequest.getId(),
                        trustedAuthorityCreationRequest.getCertificateId()
                )
        );

        return getAuthorityInformation();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public AuthorityInformationResponse deleteTrustedAuthority(@PathVariable("id") String trustedAuthorityId) {
        trustedAuthorityService.deleteTrustedAuthority(trustedAuthorityId);

        return getAuthorityInformation();
    }
}
