package de.tuberlin.tfdacmacs.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;

public class UserAttributeValueKey extends SymmetricElementKey {
    public UserAttributeValueKey(@NonNull Element secretKey) {
        super(secretKey);
    }

    public UserAttributeValueKey update(@NonNull UserAttributeValueUpdateKey userAttributeValueUpdateKey) {
        Element newKey = getSecretKey().duplicate().mul(userAttributeValueUpdateKey.getUpdateKey());
        return new UserAttributeValueKey(newKey);
    }
}
