package de.tuberlin.tfdacmacs.centralserver.user;

import de.tuberlin.tfdacmacs.basics.crypto.rsa.converter.KeyConverter;
import de.tuberlin.tfdacmacs.basics.exceptions.BadRequestException;
import de.tuberlin.tfdacmacs.basics.exceptions.ServiceException;
import de.tuberlin.tfdacmacs.basics.user.data.dto.UserCreationRequest;
import de.tuberlin.tfdacmacs.basics.user.data.dto.UserCreationResponse;
import de.tuberlin.tfdacmacs.basics.crypto.rsa.certificate.CertificateService;
import de.tuberlin.tfdacmacs.centralserver.user.data.User;
import de.tuberlin.tfdacmacs.centralserver.user.dto.CertificateRequest;
import de.tuberlin.tfdacmacs.centralserver.user.dto.CertificateResponse;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CertificateService certificateService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    // TODO: secure for AA
    public UserCreationResponse createUser(@Valid @RequestBody UserCreationRequest userCreationRequest) {
        if(userService.existUser(userCreationRequest.getEmail())) {
            throw new ServiceException("User with email '%s' does exist.", HttpStatus.PRECONDITION_FAILED,
                    userCreationRequest.getEmail());
        }

        User user = userService.createUser(userCreationRequest.getEmail());
        return new UserCreationResponse(user.getId());
    }

    @PutMapping("/{id}")
    public CertificateResponse signingRequest(
            @PathVariable("id") String id,
            @Valid @RequestBody CertificateRequest certificateRequest) throws CertificateEncodingException {
        try {
            PKCS10CertificationRequest pkcs10CertificationRequest = new PKCS10CertificationRequest(
                    KeyConverter.from(certificateRequest.getCertificateRequest()).toByes());
            PublicKey publicKey = KeyConverter.from(certificateRequest.getPublicKey()).toPublicKey();

            X509Certificate certificate = certificateService.certificateRequest(id, pkcs10CertificationRequest, publicKey);

            return new CertificateResponse(KeyConverter.from(certificate.getEncoded()).toBase64());
        } catch (IOException e) {
            throw new BadRequestException("Certificate request was not a valid PKCS10CertificationRequest!", e);
        }
    }
}
