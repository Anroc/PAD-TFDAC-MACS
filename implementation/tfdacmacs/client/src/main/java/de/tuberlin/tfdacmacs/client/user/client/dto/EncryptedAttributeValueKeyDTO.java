package de.tuberlin.tfdacmacs.client.user.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EncryptedAttributeValueKeyDTO {

    @NotBlank
    private String attributeValueId;
    @Min(0)
    private long version;
    @NotBlank
    private String encryptedKey;
}
