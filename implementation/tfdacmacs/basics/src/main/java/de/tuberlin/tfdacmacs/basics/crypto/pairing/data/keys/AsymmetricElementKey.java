package de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public abstract class AsymmetricElementKey {

    private final @NonNull Private privateKey;
    private final @NonNull Public publicKey;

    public AsymmetricElementKey(
            @NonNull Element privateKey,
            @NonNull Element publicKey) {
        this.privateKey = new Private(privateKey);
        this.publicKey = new Public(publicKey);
    }

    @RequiredArgsConstructor
    public class Private {
        private final @NonNull Element key;

        public Element getKey() {
            return this.key.getImmutable();
        }
    }

    @RequiredArgsConstructor
    public class Public {
        private final @NonNull Element key;

        public Element getKey() {
            return this.key.getImmutable();
        }
    }
}
