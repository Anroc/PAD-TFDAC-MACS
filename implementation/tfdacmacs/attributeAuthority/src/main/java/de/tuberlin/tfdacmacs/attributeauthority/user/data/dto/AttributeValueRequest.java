package de.tuberlin.tfdacmacs.attributeauthority.user.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributeValueRequest {

    @NotBlank
    private String attributeId;

    @NotEmpty
    private Set<Object> attributeValues;

}
