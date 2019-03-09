package de.tuberlin.tfdacmacs.crypto.pairing.data.keys;

import de.tuberlin.tfdacmacs.crypto.pairing.data.VersionedID;
import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class CipherText2FAUpdateKey extends UpdateKey {
    private final VersionedID attributeValueId;
    private final VersionedID oid;

    public CipherText2FAUpdateKey(@NonNull Element updateKey, @NonNull VersionedID attributeValueId, @NonNull String oid, long targetVersion) {
        super(updateKey, targetVersion);
        this.attributeValueId = attributeValueId;
        this.oid = new VersionedID(oid, targetVersion);
    }
}
