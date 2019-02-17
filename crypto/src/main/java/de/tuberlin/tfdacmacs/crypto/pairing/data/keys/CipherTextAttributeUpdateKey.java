package de.tuberlin.tfdacmacs.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class CipherTextAttributeUpdateKey extends UpdateKey {

    private final String attributeValueId;
    private final AttributeValueKey.Public newAttributeValuePublicKey;

    public CipherTextAttributeUpdateKey(
            @NonNull Element updateKey,
            @NonNull String attributeValueId,
            @NonNull AttributeValueKey.Public newAttributeValuePublicKey,
            int targetVersion) {

        super(updateKey, targetVersion);
        this.attributeValueId = attributeValueId;
        this.newAttributeValuePublicKey = newAttributeValuePublicKey;
    }
}
