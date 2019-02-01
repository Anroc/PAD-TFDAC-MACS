package de.tuberlin.tfdacmacs.client.register;

import de.tuberlin.tfdacmacs.client.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.client.register.events.LogoutEvent;
import de.tuberlin.tfdacmacs.client.register.events.SessionCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import de.tuberlin.tfdacmacs.client.keypair.data.KeyPair;

@Component
public class SessionContainer implements Session {

    private String email;
    private Certificate certificate;
    private KeyPair keyPair;

    @EventListener(SessionCreatedEvent.class)
    public void updateSession(SessionCreatedEvent sessionCreatedEvent) {
        this.email = sessionCreatedEvent.getEmail();
        this.certificate = sessionCreatedEvent.getCertificate();
        this.keyPair = sessionCreatedEvent.getKeyPair();
    }

    @EventListener(LogoutEvent.class)
    public void clearSession() {
        this.email = null;
        this.certificate = null;
        this.keyPair = null;
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
