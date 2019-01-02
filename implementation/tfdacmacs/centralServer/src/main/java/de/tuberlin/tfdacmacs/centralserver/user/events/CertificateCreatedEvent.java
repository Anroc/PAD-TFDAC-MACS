package de.tuberlin.tfdacmacs.centralserver.user.events;

import de.tuberlin.tfdacmacs.lib.events.DomainEvent;
import de.tuberlin.tfdacmacs.centralserver.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.centralserver.user.data.User;
import lombok.Data;

@Data
public class CertificateCreatedEvent extends DomainEvent<User> {

    private final Certificate certificate;

    public CertificateCreatedEvent(User user, Certificate certificate) {
        super(user);
        this.certificate = certificate;
    }
}
