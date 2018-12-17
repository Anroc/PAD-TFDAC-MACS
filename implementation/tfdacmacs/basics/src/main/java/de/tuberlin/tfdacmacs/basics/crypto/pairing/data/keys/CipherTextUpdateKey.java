package de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.NonNull;

@Data
public class CipherTextUpdateKey extends UpdateKey {

    private final String attributeValueId;
    private final AttributeValueKey.Public newAttributeValuePublicKey;

    public CipherTextUpdateKey(
            @NonNull Element updateKey,
            @NonNull String attributeValueId,
            @NonNull AttributeValueKey.Public newAttributeValuePublicKey) {

        super(updateKey);
        this.attributeValueId = attributeValueId;
        this.newAttributeValuePublicKey = newAttributeValuePublicKey;
    }
}
