package de.tuberlin.tfdacmacs.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;

public class AuthorityKey extends AsymmetricElementKey<AuthorityKey> {

    public AuthorityKey(@NonNull Element privateKey, @NonNull Element publicKey, long version) {
        super(privateKey, publicKey, version);
    }
}
