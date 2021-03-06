package de.tuberlin.tfdacmacs.dto.attributeauthority.attribute;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicAttributeValueResponse {

    @NotBlank
    private String publicKey;
    @NotNull
    private Object value;

}
