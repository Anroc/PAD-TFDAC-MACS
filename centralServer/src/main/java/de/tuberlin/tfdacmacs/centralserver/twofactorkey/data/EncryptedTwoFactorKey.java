package de.tuberlin.tfdacmacs.centralserver.twofactorkey.data;

import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.TwoFactorUpdateKey;
import de.tuberlin.tfdacmacs.lib.db.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EncryptedTwoFactorKey extends Entity {

    @NotBlank
    private String userId;

    @NotBlank
    private String dataOwnerId;

    @NotNull
    private Map<String, EncryptedTwoFactorDeviceKey> encryptedTwoFactorKeys;

    @NotNull
    private List<TwoFactorUpdateKey> twoFactorUpdateKeys = new ArrayList<>();

}
