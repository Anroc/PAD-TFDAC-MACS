package de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;

public class TwoFactorKey extends AsymmetricElementKey<TwoFactorKey> {
    public TwoFactorKey(@NonNull Element privateKey, @NonNull Element publicKey) {
        super(privateKey, publicKey);
    }

    public class Public extends AsymmetricElementKey.Public {
        public Public(@NonNull Element key) {
            super(key);
        }

        public Public update(@NonNull TwoFactorUpdateKey twoFactorUpdateKey) {
            Element newPublicKey = getKey().duplicate().mul(twoFactorUpdateKey.getUpdateKey());
            return new Public(newPublicKey);
        }
    }
}
