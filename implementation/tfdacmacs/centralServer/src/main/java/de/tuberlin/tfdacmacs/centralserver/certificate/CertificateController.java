package de.tuberlin.tfdacmacs.centralserver.certificate;

import de.tuberlin.tfdacmacs.lib.certificate.data.dto.CertificateRequest;
import de.tuberlin.tfdacmacs.lib.certificate.data.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;
import de.tuberlin.tfdacmacs.lib.exceptions.BadRequestException;
import de.tuberlin.tfdacmacs.lib.exceptions.NotFoundException;
import de.tuberlin.tfdacmacs.centralserver.certificate.data.Certificate;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;

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
                KeyConverter.from(certificate.getCertificate()).toBase64()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CertificateResponse signingRequest(@Valid @RequestBody CertificateRequest certificateRequest) {
        try {
            PKCS10CertificationRequest pkcs10CertificationRequest = new PKCS10CertificationRequest(
                    KeyConverter.from(certificateRequest.getCertificateRequest()).toByes());

            Certificate certificate = certificateService.certificateRequestUser(pkcs10CertificationRequest);

            return new CertificateResponse(certificate.getId(), KeyConverter.from(certificate.getCertificate()).toBase64());
        } catch (IOException e) {
            throw new BadRequestException("Certificate request was not a valid PKCS10CertificationRequest!", e);
        }
    }
}
