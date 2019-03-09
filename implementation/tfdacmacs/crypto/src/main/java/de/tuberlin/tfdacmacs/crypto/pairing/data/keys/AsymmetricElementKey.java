package de.tuberlin.tfdacmacs.crypto.pairing.data.keys;

import de.tuberlin.tfdacmacs.crypto.pairing.data.Versioned;
import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public abstract class AsymmetricElementKey<T> implements Versioned {

    private @NotNull @NonNull Private<T> privateKey;
    private @NotNull @NonNull Public<T> publicKey;
    private @Min(0) int version;

    public AsymmetricElementKey(
            @NonNull Element privateKey,
            @NonNull Element publicKey) {
        this(new Private(privateKey), new Public(publicKey), 0);
    }

    public AsymmetricElementKey(Private<T> privateKey, Public<T> publicKey, int version) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.version = version;

        assertVersionConsistency();
    }

    private final void assertVersionConsistency() {
        if(getPrivateKey().getVersion() != getPublicKey().getVersion() || getPublicKey().getVersion() != getVersion()) {
            throw new IllegalArgumentException("Version must be consistent over all entries.");
        }
    }

    @SuppressWarnings("unused")
    public static class Private<T> extends ElementKey {
        public Private(@NonNull Element key) {
            super(key);
        }
    }

    @SuppressWarnings("unused")
    @NoArgsConstructor
    public static class Public<T> extends ElementKey {
        public Public(@NonNull Element key) {
            super(key);
        }
    }
}
