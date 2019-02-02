package de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorKeyRequest {

    @NotBlank
    private String userId;
    @NotBlank
    private String encryptedTwoFactorKey;
}
