package de.tuberlin.tfdacmacs.attributeauthority.user.client;

import de.tuberlin.tfdacmacs.attributeauthority.feign.CAClient;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.User;
import de.tuberlin.tfdacmacs.basics.certificate.data.dto.InitCertificateRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserClient {

    private final CAClient caClient;

    public void createUserForCA(@NonNull User user) {
        log.info("Requesting user creation on CA site.");
        caClient.createUser(new InitCertificateRequest(user.getId()));
    }
}
