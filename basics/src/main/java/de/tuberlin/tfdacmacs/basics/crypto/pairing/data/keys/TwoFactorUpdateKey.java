package de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;

public class TwoFactorUpdateKey extends UpdateKey {
    public TwoFactorUpdateKey(@NonNull Element updateKey) {
        super(updateKey);
    }
}
