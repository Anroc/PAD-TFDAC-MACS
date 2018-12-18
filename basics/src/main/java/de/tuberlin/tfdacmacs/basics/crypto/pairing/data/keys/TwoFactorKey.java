package de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;

public class TwoFactorKey extends AsymmetricElementKey<TwoFactorKey> {
    public TwoFactorKey(@NonNull Element privateKey, @NonNull Element publicKey) {
        super(privateKey, publicKey);
    }
}
