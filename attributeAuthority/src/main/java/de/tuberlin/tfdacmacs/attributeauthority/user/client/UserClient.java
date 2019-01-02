package de.tuberlin.tfdacmacs.attributeauthority.user.client;

import de.tuberlin.tfdacmacs.attributeauthority.config.AttributeAuthorityConfig;
import de.tuberlin.tfdacmacs.attributeauthority.client.CAClient;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.User;
import de.tuberlin.tfdacmacs.basics.user.data.dto.UserCreationRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserClient {

    private final CAClient caClient;
    private final AttributeAuthorityConfig config;

    public void createUserForCA(@NonNull User user) {
        log.info("Requesting user creation on CA site.");
        caClient.createUser(new UserCreationRequest(user.getId(), config.getId()));
    }
}
