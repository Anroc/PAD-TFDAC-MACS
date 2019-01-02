package de.tuberlin.tfdacmacs.lib.user.data.dto;

import de.tuberlin.tfdacmacs.lib.user.data.DeviceState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceResponse {

    private String certificateId;
    private DeviceState deviceState;
    private Set<EncryptedAttributeValueKeyDTO> encryptedAttributeValueKeys;

}
