package de.tuberlin.tfdacmacs.centralserver.authority;

import de.tuberlin.tfdacmacs.centralserver.authority.data.AttributeAuthority;
import de.tuberlin.tfdacmacs.centralserver.certificate.CertificateService;
import de.tuberlin.tfdacmacs.centralserver.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.centralserver.security.AuthenticationFacade;
import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AuthorityKey;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;
import de.tuberlin.tfdacmacs.lib.authority.AttributeAuthorityPublicKeyRequest;
import de.tuberlin.tfdacmacs.lib.authority.AttributeAuthorityResponse;
import de.tuberlin.tfdacmacs.lib.certificate.data.dto.CertificateRequest;
import de.tuberlin.tfdacmacs.lib.exceptions.BadRequestException;
import de.tuberlin.tfdacmacs.lib.exceptions.NotFoundException;
import de.tuberlin.tfdacmacs.lib.exceptions.ServiceException;
import de.tuberlin.tfdacmacs.lib.gpp.GlobalPublicParameterProvider;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/authorities")
public class AttributeAuthorityController {

    private final CertificateService certificateService;
    private final AttributeAuthorityService attributeAuthorityService;
    private final AuthenticationFacade authenticationFacade;
    private final GlobalPublicParameterProvider globalPublicParameterProvider;

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

    @PutMapping("/{id}/public-key")
    @PreAuthorize("hasRole('ROLE_AUTHORITY')")
    public AttributeAuthorityResponse updateAuthorityPublicKey(
            @PathVariable("id") String authorityId,
            @RequestBody @Valid AttributeAuthorityPublicKeyRequest attributeAuthorityPublicKeyRequest) {
        String requestingId = authenticationFacade.getId();
        if(! requestingId.equals(authorityId)) {
            throw new ServiceException("Not your authority!", HttpStatus.FORBIDDEN);
        }

        AttributeAuthority attributeAuthority = attributeAuthorityService.findEntity(authorityId)
                .orElseThrow(() -> new NotFoundException(authorityId));

        AuthorityKey.Public publicKey = new AuthorityKey.Public<>(ElementConverter.convert(
                attributeAuthorityPublicKeyRequest.getAuthorityPublicKey(),
                globalPublicParameterProvider.getGlobalPublicParameter().g1()
        ), attributeAuthorityPublicKeyRequest.getVersion());

        attributeAuthority.setPublicKey(publicKey);
        attributeAuthority.setSignature(attributeAuthorityPublicKeyRequest.getSignature());
        attributeAuthority = attributeAuthorityService.updateAttributeAuthority(attributeAuthority);

        return buildAttributeAuthorityResponse(attributeAuthority);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_AUTHORITY', 'ROLE_USER')")
    public AttributeAuthorityResponse getAttributeAuthority(@PathVariable("id") String id) {
        return attributeAuthorityService.findEntity(id)
                .map(this::buildAttributeAuthorityResponse)
                .orElseThrow(() -> new NotFoundException(id));
    }

    private AttributeAuthorityResponse buildAttributeAuthorityResponse(AttributeAuthority attributeAuthority) {
        return new AttributeAuthorityResponse(
                attributeAuthority.getId(),
                attributeAuthority.getCertificateId(),
                Optional.ofNullable(attributeAuthority.getPublicKey())
                    .map(AuthorityKey.Public::getKey)
                    .map(ElementConverter::convert)
                    .orElse(null),
                attributeAuthority.getPublicKey().getVersion(),
                attributeAuthority.getSignature()
        );
    }
}
