package de.tuberlin.tfdacmacs.centralserver.twofactorkey.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EncryptedTwoFactorDeviceKey {

    @NotBlank
    private String encryptedSymmetricKey;
    @NotBlank
    private String encryptedKey;
    @Min(0)
    private long version;
}

