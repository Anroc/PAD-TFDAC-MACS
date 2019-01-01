package de.tuberlin.tfdacmacs.centralserver.user.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.tuberlin.tfdacmacs.basics.db.Entity;
import de.tuberlin.tfdacmacs.centralserver.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.centralserver.user.events.CertificateCreatedEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User extends Entity {

    @NotBlank
    private String authorityId;

    @NotNull
    private Set<Device> devices = new HashSet<>();

    public User(@NonNull String id, @NotBlank String authorityId) {
        super(id);
        this.authorityId = authorityId;
    }

    public User addNewDevice(@NonNull Certificate certificate) {
        if(! getCertificateIds().contains(certificate.getId())) {
            devices.add(new Device(certificate.getId()));
            registerDomainEvent(new CertificateCreatedEvent(this, certificate));
        }
        return this;
    }

    @JsonIgnore
    public Set<String> getCertificateIds() {
        return this.devices.stream().map(Device::getCertificateId).collect(Collectors.toSet());
    }
}
