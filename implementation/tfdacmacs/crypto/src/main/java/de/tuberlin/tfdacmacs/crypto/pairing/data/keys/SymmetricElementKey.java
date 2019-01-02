package de.tuberlin.tfdacmacs.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotNull;

@RequiredArgsConstructor
public abstract class SymmetricElementKey {

    @NotNull
    private final @NonNull Element secretKey;

    public Element getSecretKey() {
        return this.secretKey.getImmutable();
    }
}
