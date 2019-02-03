package de.tuberlin.tfdacmacs.client.twofactor.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorKeyRequest {

    @NotBlank
    private String userId;
    @NotNull
    private Map<String, EncryptedTwoFactorDeviceKeyDTO> encryptedTwoFactorKeys;
}
