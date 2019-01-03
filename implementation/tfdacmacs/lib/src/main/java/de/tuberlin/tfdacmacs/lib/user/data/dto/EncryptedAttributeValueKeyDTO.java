package de.tuberlin.tfdacmacs.lib.user.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EncryptedAttributeValueKeyDTO {

    @NotBlank
    private String attributeValueId;
    @NotBlank
    private String encryptedKey;
}
