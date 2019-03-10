package de.tuberlin.tfdacmacs.lib.user.data.dto;

import de.tuberlin.tfdacmacs.crypto.pairing.data.Versioned;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EncryptedAttributeValueKeyDTO implements Versioned {

    @NotBlank
    private String attributeValueId;
    @Min(0)
    private long version;
    @NotBlank
    private String encryptedKey;
}
