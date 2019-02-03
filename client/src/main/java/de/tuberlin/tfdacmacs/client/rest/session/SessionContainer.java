package de.tuberlin.tfdacmacs.client.rest.session;

import de.tuberlin.tfdacmacs.client.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.client.keypair.data.KeyPair;
import de.tuberlin.tfdacmacs.client.register.events.LogoutEvent;
import de.tuberlin.tfdacmacs.client.register.events.SessionInitializedEvent;
import de.tuberlin.tfdacmacs.client.rest.session.events.SessionDestroyedEvent;
import de.tuberlin.tfdacmacs.client.rest.session.events.SessionReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SessionContainer implements Session {

    private String email;
    private Certificate certificate;
    private KeyPair keyPair;

    @EventListener(SessionInitializedEvent.class)
    public SessionReadyEvent updateSession(SessionInitializedEvent sessionInitializedEvent) {
        this.email = sessionInitializedEvent.getEmail();
        this.certificate = sessionInitializedEvent.getCertificate();
        this.keyPair = sessionInitializedEvent.getKeyPair();
        return new SessionReadyEvent(this);
    }

    @EventListener(LogoutEvent.class)
    public SessionDestroyedEvent clearSession() {
        this.email = null;
        this.certificate = null;
        this.keyPair = null;
        return new SessionDestroyedEvent(this);
    }

    @Override
    public String getEmail() {
        checkIfPresent(email);
        return this.email;
    }

    @Override
    public Certificate getCertificate() {
        checkIfPresent(this.certificate);
        return this.certificate;
    }

    @Override
    public KeyPair getKeyPair() {
        checkIfPresent(this.keyPair);
        return this.keyPair;
    }

    @Override
    public boolean isActive() {
        return email != null;
    }

    private void checkIfPresent(Object value) {
        if(value == null) {
            throw new IllegalStateException("Session is not yet initialized.");
        }
    }
}
