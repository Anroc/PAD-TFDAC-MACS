package de.tuberlin.tfdacmacs.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;

public class UserAttributeValueUpdateKey extends UpdateKey {
    public UserAttributeValueUpdateKey(@NonNull Element updateKey) {
        super(updateKey);
    }
}
