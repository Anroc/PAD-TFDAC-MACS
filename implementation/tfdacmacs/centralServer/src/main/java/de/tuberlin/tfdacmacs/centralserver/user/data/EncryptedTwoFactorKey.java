package de.tuberlin.tfdacmacs.centralserver.user.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EncryptedTwoFactorKey {

    @NotBlank
    private String dataOwnerId;

    @NotBlank
    private String encryptedKey;

}
