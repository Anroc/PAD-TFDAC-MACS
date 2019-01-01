package de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.*;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public abstract class AsymmetricElementKey<T> {

    private @NotNull @NonNull Private<T> privateKey;
    private @NotNull @NonNull Public<T> publicKey;

    public AsymmetricElementKey(
            @NonNull Element privateKey,
            @NonNull Element publicKey) {
        this.privateKey = new Private(privateKey);
        this.publicKey = new Public(publicKey);
    }

    @SuppressWarnings("unused")
    public static class Private<T> extends Key {
        public Private(@NonNull Element key) {
            super(key);
        }
    }

    @SuppressWarnings("unused")
    public static class Public<T> extends Key {
        public Public(@NonNull Element key) {
            super(key);
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    private static class Key {
        private @NotNull @NonNull Element key;

        public Element getKey() {
            return this.key.getImmutable();
        }
    }
}