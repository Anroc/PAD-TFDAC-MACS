package de.tuberlin.tfdacmacs.client.register;

import de.tuberlin.tfdacmacs.client.attribute.AttributeService;
import de.tuberlin.tfdacmacs.client.certificate.CertificateService;
import de.tuberlin.tfdacmacs.client.certificate.data.Certificate;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
@RequiredArgsConstructor
public class RegisterCommand {

    private final CertificateService certificateService;
    private final AttributeService attributeService;

    @ShellMethod("Register this client")
    public void register(String email) {
        Certificate certificate = certificateService.certificateRequest(email);
        certificateService.generateP12KeyStore(certificate);
        attributeService.retrieveAttributes(email, certificate.getId());
        System.out.println(String.format("Successfully registered as user [%s]", email));
    }
}
