package de.tuberlin.tfdacmacs.lib.user.data.dto;

import de.tuberlin.tfdacmacs.lib.user.data.DeviceState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceUpdateRequest {

    @NotNull
    private DeviceState deviceState;

    @NotBlank
    private String encryptedKey;

    @Valid
    @NotEmpty
    private Set<EncryptedAttributeValueKeyDTO> encryptedAttributeValueKeys;
}
