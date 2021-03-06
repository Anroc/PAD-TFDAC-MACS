package de.tuberlin.tfdacmacs.client.register;

import de.tuberlin.tfdacmacs.client.attribute.AttributeService;
import de.tuberlin.tfdacmacs.client.certificate.CertificateService;
import de.tuberlin.tfdacmacs.client.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.client.config.ClientConfig;
import de.tuberlin.tfdacmacs.client.config.StandardStreams;
import de.tuberlin.tfdacmacs.client.db.CRUDOperations;
import de.tuberlin.tfdacmacs.client.keypair.KeyPairService;
import de.tuberlin.tfdacmacs.client.register.events.LoginEvent;
import de.tuberlin.tfdacmacs.client.register.events.LogoutEvent;
import de.tuberlin.tfdacmacs.client.register.events.RegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@ShellComponent
@RequiredArgsConstructor
public class RegisterCommand {

    private final CertificateService certificateService;
    private final AttributeService attributeService;
    private final KeyPairService keyPairService;
    private final ApplicationEventPublisher publisher;

    private final ApplicationContext context;
    private final ClientConfig clientConfig;
    private final StandardStreams standardStreams;

    @ShellMethod("Register this client")
    public void register(String email) {
        Certificate certificate = certificateService.certificateRequest(email);
        certificateService.generateP12KeyStore(certificate);
        publisher.publishEvent(new RegisteredEvent(email, certificate, keyPairService.getKeyPair(email)));
        attributeService.retrieveAttributesForUser(email, certificate.getId());
        standardStreams.out(String.format("Successfully registered as user [%s]", email));
    }

    @ShellMethod("Login from the given files")
    public void login(String email) {
        Certificate certificate = certificateService.login(email);
        publisher.publishEvent(new LoginEvent(email, certificate, keyPairService.getKeyPair(email)));
    }

    @ShellMethod("Resets this client")
    public void reset() throws IOException {
        context.getBeansOfType(CRUDOperations.class).values().forEach(CRUDOperations::drop);
        File p12Dir = Paths.get(clientConfig.getP12Certificate().getLocation()).toFile();
        if(p12Dir.exists()) {
            FileUtils.cleanDirectory(p12Dir);
        }
        publisher.publishEvent(new LogoutEvent("Reset was called."));
    }

    @ShellMethod("Log this client out")
    public void logout() {
        publisher.publishEvent(new LogoutEvent("Logout was called."));
    }
}
