package de.tuberlin.tfdacmacs.client.twofactor.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwoFactorKeyCreationRequest {

    @NotBlank
    private String dataOwnerId;
    @NotBlank
    private String encryptedTwoFactorKey;
}
