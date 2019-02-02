package de.tuberlin.tfdacmacs.centralserver.twofactorkey.data;

import de.tuberlin.tfdacmacs.lib.db.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EncryptedTwoFactorKey extends Entity {

    @NotBlank
    private String userId;

    @NotBlank
    private String dataOwnerId;

    @NotBlank
    private String encryptedKey;

}
