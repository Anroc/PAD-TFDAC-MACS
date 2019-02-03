package de.tuberlin.tfdacmacs.client.twofactor.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EncryptedTwoFactorDeviceKeyDTO {

    @NotBlank
    private String encryptedSymmetricKey;
    @NotBlank
    private String encryptedKey;

}
