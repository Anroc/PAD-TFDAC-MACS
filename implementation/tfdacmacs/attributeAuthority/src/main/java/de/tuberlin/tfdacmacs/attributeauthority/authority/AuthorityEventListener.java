package de.tuberlin.tfdacmacs.attributeauthority.authority;

import de.tuberlin.tfdacmacs.attributeauthority.authority.client.AuthorityClient;
import de.tuberlin.tfdacmacs.attributeauthority.authority.events.AuthorityKeyCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthorityEventListener {

    private final AuthorityClient authorityClient;

    @EventListener
    public void handleAuthorityKeysCreatedEvent(AuthorityKeyCreatedEvent authorityKeyCreatedEvent) {
        authorityClient.uploadAuthorityPublicKey(authorityKeyCreatedEvent.getSource().getPublicKey());
    }
}
