package de.tuberlin.tfdacmacs.lib.attributes.data.dto;

import de.tuberlin.tfdacmacs.lib.attributes.data.AbstractAttribute;
import de.tuberlin.tfdacmacs.lib.attributes.data.AttributeType;
import de.tuberlin.tfdacmacs.lib.attributes.data.AttributeValueComponent;
import de.tuberlin.tfdacmacs.lib.attributes.data.PublicAttributeValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

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

    public static PublicAttributeResponse from(@NonNull AbstractAttribute<? extends AttributeValueComponent> attribute) {
        PublicAttributeResponse publicAttributeResponse = new PublicAttributeResponse();
        publicAttributeResponse.setAuthorityDomain(attribute.getAuthorityDomain());
        publicAttributeResponse.setName(attribute.getName());
        publicAttributeResponse.setValues(
                attribute.getValues().stream()
                        .map(attributeValue -> {
                            String signature = null;
                            if(attributeValue instanceof PublicAttributeValue) {
                                signature = ((PublicAttributeValue) attributeValue).getSignature();
                            }
                            return PublicAttributeValueResponse.from(attributeValue, signature);
                        }).collect(Collectors.toList())
        );
        publicAttributeResponse.setType(attribute.getType());
        publicAttributeResponse.setId(attribute.getId());
        return publicAttributeResponse;
    }
}
