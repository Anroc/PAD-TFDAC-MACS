package de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public abstract class AsymmetricElementKey<T> {

    private final @NonNull Private<T> privateKey;
    private final @NonNull Public<T> publicKey;

    public AsymmetricElementKey(
            @NonNull Element privateKey,
            @NonNull Element publicKey) {
        this.privateKey = new Private(privateKey);
        this.publicKey = new Public(publicKey);
    }

    @SuppressWarnings("unused")
    public class Private<T> extends Key {
        public Private(@NonNull Element key) {
            super(key);
        }
    }

    @SuppressWarnings("unused")
    public class Public<T> extends Key {
        public Public(@NonNull Element key) {
            super(key);
        }
    }

    @RequiredArgsConstructor
    private class Key {
        private final @NonNull Element key;

        public Element getKey() {
            return this.key.getImmutable();
        }
    }
}
