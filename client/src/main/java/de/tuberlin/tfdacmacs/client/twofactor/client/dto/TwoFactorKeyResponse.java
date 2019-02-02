package de.tuberlin.tfdacmacs.client.twofactor.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorKeyResponse {

    @NotBlank
    private String id;
    @NotBlank
    private String userId;
    @NotBlank
    private String ownerId;
    @NotNull
    private Map<String, String> encryptedTwoFactorKeys;
}
