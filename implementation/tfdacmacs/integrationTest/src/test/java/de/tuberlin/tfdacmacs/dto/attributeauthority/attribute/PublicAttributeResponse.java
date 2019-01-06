package de.tuberlin.tfdacmacs.dto.attributeauthority.attribute;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicAttributeResponse {

    @NotBlank
    private String authorityDomain;
    @NotBlank
    private String name;
    @NotBlank
    private List<PublicAttributeValueResponse> values;
    @NotNull
    private AttributeType type;
    @NotBlank
    private String id;

}
