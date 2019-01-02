package de.tuberlin.tfdacmacs.attributeauthority.user.events;

import de.tuberlin.tfdacmacs.attributeauthority.user.data.User;
import de.tuberlin.tfdacmacs.lib.events.DomainEvent;
import lombok.Data;

import java.security.cert.X509Certificate;

@Data
public class DeviceApprovedEvent extends DomainEvent<User> {

    private final String deviceId;
    private final X509Certificate x509Certificate;

    public DeviceApprovedEvent(User user, String deviceId, X509Certificate x509Certificate) {
        super(user);
        this.deviceId = deviceId;
        this.x509Certificate = x509Certificate;
    }
}
