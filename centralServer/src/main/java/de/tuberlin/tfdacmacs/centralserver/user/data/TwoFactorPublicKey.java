package de.tuberlin.tfdacmacs.centralserver.user.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwoFactorPublicKey {

    @NotBlank
    private String twoFactorAuthenticationPublicKey;

    @NotBlank
    private String signature;
}
