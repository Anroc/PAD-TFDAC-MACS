package de.tuberlin.tfdacmacs.basics.crypto.pairing.data;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
public class DataOwner {

    private final String id;
    private final Element twoFactorPrivateKey;

    public DataOwner(String id, Element twoFactorPrivateKey) {
        this.id = id;
        this.twoFactorPrivateKey = twoFactorPrivateKey.getImmutable();
    }
}
