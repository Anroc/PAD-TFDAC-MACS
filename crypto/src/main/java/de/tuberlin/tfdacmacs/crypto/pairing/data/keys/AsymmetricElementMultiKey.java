package de.tuberlin.tfdacmacs.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class AsymmetricElementMultiKey<T> extends ElementKey {

    @NotNull
    protected Map<T, Element> publicKeys;

    public AsymmetricElementMultiKey(@NonNull Element privateKey, long version) {
        super(privateKey, version);
        this.publicKeys = new HashMap<>();
    }

    public AsymmetricElementMultiKey putPublicKey(@NonNull T id, @NonNull Element publicKey) {
        this.publicKeys.put(id, publicKey);
        return this;
    }
}
