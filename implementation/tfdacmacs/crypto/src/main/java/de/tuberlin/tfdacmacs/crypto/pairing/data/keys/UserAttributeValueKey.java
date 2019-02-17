package de.tuberlin.tfdacmacs.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;

public class UserAttributeValueKey extends ElementKey {
    public UserAttributeValueKey(@NonNull Element key) {
        super(key);
    }

    public UserAttributeValueKey update(@NonNull UserAttributeValueUpdateKey userAttributeValueUpdateKey) {
        Element newKey = getKey().duplicate().mul(userAttributeValueUpdateKey.getUpdateKey());
        return new UserAttributeValueKey(newKey).incrementVersion();
    }
}
