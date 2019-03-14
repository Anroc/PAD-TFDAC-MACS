package de.tuberlin.tfdacmacs.client.csp.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorCipherTextUpdateKey {

    @NotBlank
    private String attributeValueId;
    @Min(0)
    private long attributeVersion;
    @NotBlank
    private String updateKey;

}
