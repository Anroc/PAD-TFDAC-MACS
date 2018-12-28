package de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class CipherText2FAUpdateKey extends UpdateKey {
    private final String attributeValueId;
    private final String oid;

    public CipherText2FAUpdateKey(@NonNull Element updateKey, @NonNull String attributeValueId, @NonNull String oid) {

        super(updateKey);
        this.attributeValueId = attributeValueId;
        this.oid = oid;
    }
}
