package de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.dto;

import de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.EncryptedTwoFactorDeviceKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EncryptedTwoFactorDeviceKeyDTO {

    @NotBlank
    private String encryptedSymmetricKey;
    @NotBlank
    private String encryptedKey;
    @Min(0)
    private long version;

    public EncryptedTwoFactorDeviceKey toEncryptedTwoFactorDeviceKey() {
        return new EncryptedTwoFactorDeviceKey(
                getEncryptedSymmetricKey(),
                getEncryptedKey(),
                getVersion()
        );
    }

    public static EncryptedTwoFactorDeviceKeyDTO from(EncryptedTwoFactorDeviceKey encryptedTwoFactorDeviceKey) {
        return new EncryptedTwoFactorDeviceKeyDTO(
                encryptedTwoFactorDeviceKey.getEncryptedSymmetricKey(),
                encryptedTwoFactorDeviceKey.getEncryptedKey(),
                encryptedTwoFactorDeviceKey.getVersion()
        );
    }

}
