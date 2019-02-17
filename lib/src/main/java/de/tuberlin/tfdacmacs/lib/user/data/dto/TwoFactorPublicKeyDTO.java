package de.tuberlin.tfdacmacs.lib.user.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwoFactorPublicKeyDTO  {

    @NotBlank
    private String twoFactorAuthenticationPublicKey;

    @NotBlank
    // userId + twoFactorAuthencationPublicKey
    private String signature;

}
