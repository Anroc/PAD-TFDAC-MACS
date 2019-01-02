package de.tuberlin.tfdacmacs.lib.attributes.data.dto;

import de.tuberlin.tfdacmacs.lib.attributes.data.AttributeValue;
import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
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

    public static PublicAttributeValueResponse from(AttributeValue attributeValue) {
        PublicAttributeValueResponse publicAttributeValueResponse = new PublicAttributeValueResponse();
        publicAttributeValueResponse.setValue(attributeValue.getValue());
        publicAttributeValueResponse.setPublicKey(ElementConverter.convert(attributeValue.getPublicKey().getKey()));
        return publicAttributeValueResponse;
    }
}
