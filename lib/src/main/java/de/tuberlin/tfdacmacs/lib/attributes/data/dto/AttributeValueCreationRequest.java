package de.tuberlin.tfdacmacs.lib.attributes.data.dto;

import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import de.tuberlin.tfdacmacs.lib.attributes.data.AttributeType;
import de.tuberlin.tfdacmacs.lib.attributes.data.AttributeValueComponent;
import de.tuberlin.tfdacmacs.lib.attributes.data.PublicAttributeValue;
import it.unisa.dia.gas.jpbc.Field;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributeValueCreationRequest {

    @NotBlank
    private String value;

    @NotBlank
    private String publicKey;

    @Min(0)
    private long version;

    @NotBlank
    private String signature;

    public static AttributeValueCreationRequest from(AttributeValueComponent attributeValue, String signature) {
        return new AttributeValueCreationRequest(
                attributeValue.getValue().toString(),
                ElementConverter.convert(attributeValue.getPublicKeyComponent()),
                attributeValue.getVersion(),
                signature
        );
    }

    public PublicAttributeValue toAttributeValue(@NonNull Field field, @NonNull AttributeType type) {
        return new PublicAttributeValue(
                ElementConverter.convert(getPublicKey(), field),
                type.cast(value),
                getVersion(),
                getSignature()
        );
    }
}
