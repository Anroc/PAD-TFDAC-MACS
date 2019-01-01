package de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class UpdateKey {

    private final @NonNull Element updateKey;

    public Element getUpdateKey() {
        return this.updateKey.getImmutable();
    }
}