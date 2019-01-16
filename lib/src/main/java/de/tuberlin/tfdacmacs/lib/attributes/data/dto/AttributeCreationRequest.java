package de.tuberlin.tfdacmacs.lib.attributes.data.dto;

import de.tuberlin.tfdacmacs.lib.attributes.data.*;
import it.unisa.dia.gas.jpbc.Field;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttributeCreationRequest {

    @NotBlank
    private String id;
    @NotBlank
    private String name;
    @NotBlank
    private String authorityDomain;
    @NotNull
    private AttributeType type;

    @NotNull
    @NotEmpty
    private List<AttributeValueCreationRequest> values;

    public static AttributeCreationRequest from(@NonNull AbstractAttribute<? extends AttributeValueComponent> attribute) {
        return new AttributeCreationRequest(
                attribute.getId(),
                attribute.getName(),
                attribute.getAuthorityDomain(),
                attribute.getType(),
                attribute.getValues().stream()
                        .map(AttributeValueCreationRequest::from)
                        .collect(Collectors.toList())
        );
    }

    public PublicAttribute toAttribute(@NonNull Field field) {
        return AbstractAttribute.createPublicAttribute(
                getAuthorityDomain(),
                getName(),
                getValues().stream()
                    .map(value -> value.toAttributeValue(field, getType()))
                    .collect(Collectors.toSet()),
                getType()
        );
    }
}
