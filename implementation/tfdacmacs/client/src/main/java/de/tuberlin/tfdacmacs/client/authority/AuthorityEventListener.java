package de.tuberlin.tfdacmacs.client.authority;

import de.tuberlin.tfdacmacs.client.authority.client.AuthorityClient;
import de.tuberlin.tfdacmacs.client.register.events.SessionCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthorityEventListener {

    private final AuthorityClient client;
    private final AuthorityService authorityService;

    @EventListener(SessionCreatedEvent.class)
    public void retrieveTrustedAuthorities() {
        client.retrieveTrustedAuthorities()
                .forEach(authorityService::upsert);
    }
}
