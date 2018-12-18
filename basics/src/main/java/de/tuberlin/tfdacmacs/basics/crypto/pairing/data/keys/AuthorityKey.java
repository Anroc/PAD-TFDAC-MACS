package de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;

public class AuthorityKey extends AsymmetricElementKey<AuthorityKey> {

    public AuthorityKey(@NonNull Element privateKey, @NonNull Element publicKey) {
        super(privateKey, publicKey);
    }
}
