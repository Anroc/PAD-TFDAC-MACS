package de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class AttributeValueKey extends AsymmetricElementKey<AttributeValueKey> {

    private final String attributeValueId;

    public AttributeValueKey(@NonNull Element privateKey, @NonNull Element publicKey, @NonNull String attributeValueId) {
        super(privateKey, publicKey);
        this.attributeValueId = attributeValueId;
    }
}
