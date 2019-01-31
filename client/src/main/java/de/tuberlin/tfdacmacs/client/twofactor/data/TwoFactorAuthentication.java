package de.tuberlin.tfdacmacs.client.twofactor.data;

import de.tuberlin.tfdacmacs.crypto.pairing.data.DataOwner;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.TwoFactorKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwoFactorAuthentication {

    @NotBlank
    private String ownerId;
    @NotNull
    private TwoFactorKey twoFactorKey;

    public DataOwner toDataOwner() {
        return new DataOwner(
                getOwnerId(),
                getTwoFactorKey().getPrivateKey()
        );
    }
}
