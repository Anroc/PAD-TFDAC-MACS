package de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
public abstract class AsymmetricElementMultiKey<T> extends SymmetricElementKey {

    @NotNull
    protected Map<T, Element> publicKeys;

    public AsymmetricElementMultiKey(@NonNull Element privateKey) {
        super(privateKey);
        this.publicKeys = new HashMap<>();
    }

    public AsymmetricElementMultiKey putPublicKey(@NonNull T id, @NonNull Element publicKey) {
        this.publicKeys.put(id, publicKey);
        return this;
    }
}
