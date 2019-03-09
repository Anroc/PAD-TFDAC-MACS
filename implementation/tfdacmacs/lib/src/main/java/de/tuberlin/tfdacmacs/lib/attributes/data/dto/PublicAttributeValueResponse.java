package de.tuberlin.tfdacmacs.lib.attributes.data.dto;

import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import de.tuberlin.tfdacmacs.lib.attributes.data.AttributeValueComponent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
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
    @Nullable
    private String signature;
    @Min(0)
    private long version;

    public static PublicAttributeValueResponse from(@NonNull AttributeValueComponent attributeValue, @Nullable String signature) {
        PublicAttributeValueResponse publicAttributeValueResponse = new PublicAttributeValueResponse();
        publicAttributeValueResponse.setValue(attributeValue.getValue());
        publicAttributeValueResponse.setPublicKey(ElementConverter.convert(attributeValue.getPublicKeyComponent()));
        publicAttributeValueResponse.setSignature(signature);
        publicAttributeValueResponse.setVersion(attributeValue.getVersion());
        return publicAttributeValueResponse;
    }
}
