package de.tuberlin.tfdacmacs.centralserver.user.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.tuberlin.tfdacmacs.centralserver.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.centralserver.user.events.CertificateCreatedEvent;
import de.tuberlin.tfdacmacs.lib.db.Entity;
import de.tuberlin.tfdacmacs.lib.user.data.dto.AttributeValueUpdateKeyDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User extends Entity {

    @NotBlank
    private String authorityId;

    @Valid
    @Nullable
    private TwoFactorPublicKey twoFactorPublicKey;

    @Valid
    @Nullable
    private Map<String, Map<Long, AttributeValueUpdateKeyDTO>> attributeValueUpdateKeys;

    @NotNull
    @Valid
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

    @JsonIgnore
    public Optional<Device> findDevice(@NonNull String deviceId) {
        return getDevices().stream().filter(device -> device.getCertificateId().equals(deviceId)).findFirst();
    }

    public User addAttributeValueUpdateKey(@NonNull AttributeValueUpdateKeyDTO attributeValueUpdateKeyDTO) {
        if(this.attributeValueUpdateKeys == null) {
            this.attributeValueUpdateKeys = new HashMap<>();
        }

        if(this.attributeValueUpdateKeys.get(attributeValueUpdateKeyDTO.getAttributeValueId()) == null) {
            this.attributeValueUpdateKeys.put(attributeValueUpdateKeyDTO.getAttributeValueId(), new HashMap<>());
        }

        if(this.attributeValueUpdateKeys.get(attributeValueUpdateKeyDTO.getAttributeValueId()).get(attributeValueUpdateKeyDTO.getTargetVersion()) != null) {
            throw new IllegalArgumentException("Update Key already present.");
        }

        this.attributeValueUpdateKeys
                .get(attributeValueUpdateKeyDTO.getAttributeValueId())
                .put(attributeValueUpdateKeyDTO.getTargetVersion(), attributeValueUpdateKeyDTO);

        return this;
    }
}
