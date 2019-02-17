package de.tuberlin.tfdacmacs.lib.user.data.dto;

import de.tuberlin.tfdacmacs.crypto.pairing.data.Versioned;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwoFactorPublicKeyDTO implements Versioned {

    @Min(0)
    int version;

    @NotBlank
    private String twoFactorAuthenticationPublicKey;

    @NotBlank
    // userId + twoFactorAuthencationPublicKey
    private String signature;

}
