package de.tuberlin.tfdacmacs.crypto.pairing.data.keys;

import de.tuberlin.tfdacmacs.crypto.pairing.data.Versioned;
import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public abstract class AsymmetricElementKey<T> implements Versioned {

    private @NotNull @NonNull Private<T> privateKey;
    private @NotNull @NonNull Public<T> publicKey;
    private @Min(0) long version;

    public AsymmetricElementKey(
            @NonNull Element privateKey,
            @NonNull Element publicKey,
            long version) {
        this(new Private(privateKey, version), new Public(publicKey, version), version);
    }

    public AsymmetricElementKey(Private<T> privateKey, Public<T> publicKey, long version) {
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
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class Private<T> extends ElementKey {
        public Private(@NonNull Element key, long version) {
            super(key, version);
        }
    }

    @SuppressWarnings("unused")
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class Public<T> extends ElementKey {
        public Public(@NonNull Element key, long version) {
            super(key, version);
        }
    }
}
