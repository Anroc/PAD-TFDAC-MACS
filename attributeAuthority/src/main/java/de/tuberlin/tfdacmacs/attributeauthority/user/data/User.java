package de.tuberlin.tfdacmacs.attributeauthority.user.data;

import de.tuberlin.tfdacmacs.attributeauthority.user.events.DeviceApprovedEvent;
import de.tuberlin.tfdacmacs.lib.db.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@Slf4j
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends Entity {

    public User(@NonNull String id) {
        super(id);
        this.attributes = new HashSet<>();
    }

    @Valid
    @NotNull
    private Set<UserAttributeKey> attributes;

    private Map<String, X509Certificate> devices = new HashMap<>();

    private Map<String, X509Certificate> unapprovedDevices = new HashMap<>();

    public User approve(@NonNull String deviceId) {
        if(! unapprovedDevices.containsKey(deviceId)) {
            X509Certificate x509Certificate = unapprovedDevices.get(deviceId);
            devices.putIfAbsent(deviceId, x509Certificate);
            unapprovedDevices.remove(deviceId);
            registerDomainEvent(new DeviceApprovedEvent(this, deviceId, x509Certificate));
        } else {
            log.warn("User [{}] does not contain [{}] in unapprovedDevices.", getId(), deviceId);
        }
        return this;
    }
}
