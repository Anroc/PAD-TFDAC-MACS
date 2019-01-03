package de.tuberlin.tfdacmacs.attributeauthority.user.events;

import de.tuberlin.tfdacmacs.attributeauthority.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.User;
import de.tuberlin.tfdacmacs.lib.events.DomainEvent;
import lombok.Data;

@Data
public class DeviceApprovedEvent extends DomainEvent<User> {

    private final Certificate certificate;

    public DeviceApprovedEvent(User user, Certificate certificate) {
        super(user);
        this.certificate = certificate;
    }
}
