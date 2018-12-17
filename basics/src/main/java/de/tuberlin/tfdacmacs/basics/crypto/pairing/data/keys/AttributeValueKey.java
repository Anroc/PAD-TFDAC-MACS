package de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.NonNull;

@Data
public class AttributeValueKey extends AsymmetricElementKey {

    private final String attributeValueId;

    public AttributeValueKey(@NonNull Element privateKey, @NonNull Element publicKey, @NonNull String attributeValueId) {
        super(privateKey, publicKey);
        this.attributeValueId = attributeValueId;
    }
}
