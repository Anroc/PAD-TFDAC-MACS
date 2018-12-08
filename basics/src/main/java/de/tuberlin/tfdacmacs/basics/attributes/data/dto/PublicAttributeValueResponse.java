package de.tuberlin.tfdacmacs.basics.attributes.data.dto;

import de.tuberlin.tfdacmacs.basics.attributes.data.AttributeValue;
import de.tuberlin.tfdacmacs.basics.utils.ElementConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicAttributeValueResponse<T> {

    @NotBlank
    private String publicKey;
    @NotNull
    private T value;

    public static <T> PublicAttributeValueResponse from(AttributeValue<T> attributeValue) {
        PublicAttributeValueResponse publicAttributeValueResponse = new PublicAttributeValueResponse();
        publicAttributeValueResponse.setValue(attributeValue.getValue());
        publicAttributeValueResponse.setPublicKey(ElementConverter.convert(attributeValue.getPublicKey()));
        return publicAttributeValueResponse;
    }
}
