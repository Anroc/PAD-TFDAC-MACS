package de.tuberlin.tfdacmacs.attributeauthority.user;

import de.tuberlin.tfdacmacs.attributeauthority.user.client.UserClient;
import de.tuberlin.tfdacmacs.attributeauthority.user.events.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final UserClient userClient;

    @EventListener
    public void handleUserCreatedEvent(UserCreatedEvent userCreatedEvent) {
        userClient.createUserForCA(userCreatedEvent.getSource());
    }
}
