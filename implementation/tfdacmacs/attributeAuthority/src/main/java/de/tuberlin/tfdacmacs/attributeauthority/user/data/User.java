package de.tuberlin.tfdacmacs.attributeauthority.user.data;

import de.tuberlin.tfdacmacs.attributeauthority.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.attributeauthority.user.events.DeviceApprovedEvent;
import de.tuberlin.tfdacmacs.attributeauthority.user.events.UserDeletedEvent;
import de.tuberlin.tfdacmacs.lib.db.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Optional;
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

    private Set<Certificate> devices = new HashSet<>();
    private Set<Certificate> unapprovedDevices = new HashSet<>();

    public User approve(@NonNull String deviceId) {
        Optional<Certificate> certificateOptional = find(unapprovedDevices, deviceId);
        if(certificateOptional.isPresent()) {
            Certificate certificate = certificateOptional.get();
            devices.add(certificate);
            unapprovedDevices.remove(certificate);
            registerDomainEvent(new DeviceApprovedEvent(this, certificate));
        } else {
            log.warn("User [{}] does not contain [{}] in unapprovedDevices.", getId(), deviceId);
        }
        return this;
    }

    private Optional<Certificate> find(Set<Certificate> certificates, String id) {
        return certificates.stream().filter(certificate -> certificate.getId().equals(id)).findFirst();
    }

    public void delete() {
        registerDomainEvent(new UserDeletedEvent(this));
    }
}
