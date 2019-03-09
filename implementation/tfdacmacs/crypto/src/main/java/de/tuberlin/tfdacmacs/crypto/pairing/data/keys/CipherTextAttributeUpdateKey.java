package de.tuberlin.tfdacmacs.crypto.pairing.data.keys;

import de.tuberlin.tfdacmacs.crypto.pairing.data.VersionedID;
import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class CipherTextAttributeUpdateKey extends UpdateKey {

    private final VersionedID attributeValueId;
    private final VersionedID dataOwnerId;
    private final AttributeValueKey.Public newAttributeValuePublicKey;

    public CipherTextAttributeUpdateKey(
            @NonNull Element updateKey,
            @NonNull String attributeValueId,
            VersionedID dataOwnerId,
            @NonNull AttributeValueKey.Public newAttributeValuePublicKey,
            long targetVersion) {

        super(updateKey, targetVersion);
        this.dataOwnerId = dataOwnerId;
        this.attributeValueId = new VersionedID(attributeValueId, targetVersion);
        this.newAttributeValuePublicKey = newAttributeValuePublicKey;
    }
}
