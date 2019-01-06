package de.tuberlin.tfdacmacs.dto.centralauthority.user;

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

    private String encryptedKey;
    private Set<EncryptedAttributeValueKeyDTO> encryptedAttributeValueKeys;

}
