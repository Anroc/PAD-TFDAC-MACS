package de.tuberlin.tfdacmacs.client.twofactor.data;

import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.TwoFactorKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublicTwoFactorAuthentication {
    @NotBlank
    private String ownerId;
    @NotBlank
    private String userId;
    @NotNull
    private TwoFactorKey.Secret twoFactorKey;
}
