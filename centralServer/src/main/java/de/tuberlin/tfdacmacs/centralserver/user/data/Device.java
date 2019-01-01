package de.tuberlin.tfdacmacs.centralserver.user.data;

import de.tuberlin.tfdacmacs.basics.user.data.DeviceState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Device {

    @NotBlank
    private String certificateId;
    @NotNull
    private Set<EncryptedAttributeValueKey> attributeValueKeys = new HashSet<>();
    @NotNull
    private DeviceState deviceState = DeviceState.WAITING_FOR_APPROVAL;

    public Device(@NotBlank String certificateId) {
        this.certificateId = certificateId;
    }
}
