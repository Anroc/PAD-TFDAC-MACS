package de.tuberlin.tfdacmacs.basics.attributes.data.dto;

import de.tuberlin.tfdacmacs.basics.attributes.data.Attribute;
import de.tuberlin.tfdacmacs.basics.attributes.data.AttributeType;
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
public class PublicAttributeResponse<T> {

    @NotBlank
    private String authorityDomain;
    @NotBlank
    private String name;
    @NotBlank
    private List<PublicAttributeValueResponse<T>> values;
    @NotNull
    private AttributeType type;

    @NotBlank
    private String publicKey;

    public static <T> PublicAttributeResponse from(@NonNull Attribute<T> attribute) {
        PublicAttributeResponse publicAttributeResponse = new PublicAttributeResponse();
        publicAttributeResponse.setAuthorityDomain(attribute.getAuthorityDomain());
        publicAttributeResponse.setName(attribute.getName());
        publicAttributeResponse.setValues(
                attribute.getValues().stream()
                        .map(attributeValue -> PublicAttributeValueResponse.from(attributeValue))
                        .collect(Collectors.toList())
        );
        publicAttributeResponse.setType(attribute.getType());
        return publicAttributeResponse;
    }
}
