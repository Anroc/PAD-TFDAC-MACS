package de.tuberlin.tfdacmacs.basics.crypto.pairing;

import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;

public abstract class ABECrypto {

    protected Element mulOrDefault(Element target, @NonNull Element multiplier) {
        if(target == null) {
            return multiplier;
        } else {
            return target.mul(multiplier);
        }
    }
}
