package de.tuberlin.tfdacmacs.client.register.events;

import de.tuberlin.tfdacmacs.client.certificate.data.Certificate;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.context.ApplicationEvent;

import de.tuberlin.tfdacmacs.client.keypair.data.KeyPair;

@Getter
public class SessionCreatedEvent extends ApplicationEvent {

    private final Certificate certificate;
    private final KeyPair keyPair;

    public SessionCreatedEvent(@NonNull String email, @NonNull Certificate certificate, KeyPair keyPair) {
        super(email);
        this.certificate = certificate;
        this.keyPair = keyPair;
    }


    public String getEmail() {
        return (String) getSource();
    }

}
