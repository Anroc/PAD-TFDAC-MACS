package de.tuberlin.tfdacmacs.attributeauthority.attributes.data.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@NoArgsConstructor
public class StringAttribute extends AttributeCreationRequest<String> {

    @NotEmpty
    private List<String> values;
}
