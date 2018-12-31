package de.tuberlin.tfdacmacs.centralserver.certificate;

import de.tuberlin.tfdacmacs.basics.crypto.rsa.converter.KeyConverter;
import de.tuberlin.tfdacmacs.basics.exceptions.BadRequestException;
import de.tuberlin.tfdacmacs.basics.exceptions.NotFoundException;
import de.tuberlin.tfdacmacs.basics.exceptions.ServiceException;
import de.tuberlin.tfdacmacs.basics.certificate.data.dto.InitCertificateRequest;
import de.tuberlin.tfdacmacs.basics.certificate.data.dto.CertificatePreparedResponse;
import de.tuberlin.tfdacmacs.centralserver.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.basics.certificate.data.dto.CertificateRequest;
import de.tuberlin.tfdacmacs.basics.certificate.data.dto.CertificateResponse;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.security.PublicKey;

@RestController
@RequiredArgsConstructor
@RequestMapping("/certificates")
public class CertificateController {

    private final CertificateService certificateService;

    @GetMapping("/{id}")
    public CertificateResponse getCertificate(@PathVariable("id") String id) {
        Certificate certificate = certificateService.findCertificate(id).orElseThrow(
                () -> new NotFoundException(id)
        );

        return new CertificateResponse(
                certificate.getId(),
                KeyConverter.from(certificateService.getCertificateAuthorityCertificate()).toBase64()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    // TODO: secure for AA
    public CertificatePreparedResponse createUser(@Valid @RequestBody InitCertificateRequest initCertificateRequest) {
        if(certificateService.existCertificate(initCertificateRequest.getId())) {
            throw new ServiceException("Certificate with id '%s' does exist.", HttpStatus.PRECONDITION_FAILED,
                    initCertificateRequest.getId());
        }

        Certificate certificate = certificateService.prepareCertificate(initCertificateRequest.getId());
        return new CertificatePreparedResponse(certificate.getId());
    }

    @PutMapping("/{id}")
    public CertificateResponse signingRequest(
            @PathVariable("id") String id,
            @Valid @RequestBody CertificateRequest certificateRequest) {
        Certificate certificate = certificateService.findCertificate(id).orElseThrow(
                () -> new NotFoundException(id)
        );

        // TODO: what happen in case certificate is already present?

        try {
            PKCS10CertificationRequest pkcs10CertificationRequest = new PKCS10CertificationRequest(
                    KeyConverter.from(certificateRequest.getCertificateRequest()).toByes());
            PublicKey publicKey = KeyConverter.from(certificateRequest.getPublicKey()).toPublicKey();

            certificate = certificateService.certificateRequest(certificate, pkcs10CertificationRequest, publicKey);

            return new CertificateResponse(certificate.getId(), KeyConverter.from(certificate.getCertificate()).toBase64());
        } catch (IOException e) {
            throw new BadRequestException("Certificate request was not a valid PKCS10CertificationRequest!", e);
        }
    }
}
