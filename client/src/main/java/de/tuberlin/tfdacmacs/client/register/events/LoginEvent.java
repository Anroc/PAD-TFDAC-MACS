package de.tuberlin.tfdacmacs.client.register.events;

import de.tuberlin.tfdacmacs.client.certificate.data.Certificate;
import lombok.NonNull;

import de.tuberlin.tfdacmacs.client.keypair.data.KeyPair;

public class LoginEvent extends SessionCreatedEvent {

    public LoginEvent(@NonNull String email, @NonNull Certificate certificate, @NonNull KeyPair keyPair) {
        super(email, certificate, keyPair);
    }
}
