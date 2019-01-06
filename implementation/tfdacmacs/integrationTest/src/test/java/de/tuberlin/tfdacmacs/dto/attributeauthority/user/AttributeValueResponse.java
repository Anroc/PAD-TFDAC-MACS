package de.tuberlin.tfdacmacs.dto.attributeauthority.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributeValueResponse {

    @NotBlank
    private String attributeId;
    @NotNull
    private Object value;
    @NotBlank
    private String key;
}
