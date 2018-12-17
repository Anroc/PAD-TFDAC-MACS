package de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys;

import lombok.NonNull;

public class TwoFactorKey extends AsymmetricElementKey {

    public TwoFactorKey(@NonNull Private privateKey,@NonNull Public publicKey) {
        super(privateKey, publicKey);
    }
}
