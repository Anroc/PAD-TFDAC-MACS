package de.tuberlin.tfdacmacs.attributeauthority.attribute.data.dto;

import de.tuberlin.tfdacmacs.lib.attributes.data.AttributeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributeCreationRequest {

    @NotBlank
    private String name;

    @NotNull
    private AttributeType type;

    private List<Object> values;
}
