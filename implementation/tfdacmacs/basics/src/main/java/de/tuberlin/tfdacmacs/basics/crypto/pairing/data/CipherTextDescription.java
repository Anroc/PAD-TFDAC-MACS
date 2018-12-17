package de.tuberlin.tfdacmacs.basics.crypto.pairing.data;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.NonNull;

import java.util.Set;

@Data
public class CipherTextDescription extends CipherText {

    private final Element key;

    public CipherTextDescription(Element c1, Element c2, Element c3, Set<String> accessPolicy,
            String ownerId, String encryptedMessage, Element key) {
        super(c1, c2, c3, accessPolicy, ownerId, encryptedMessage);
        this.key = key.getImmutable();
    }

    public CipherTextDescription(Element c1, Element c2, Element c3, Set<String> accessPolicy,
            String encryptedMessage, Element key) {
        super(c1, c2, c3, accessPolicy, encryptedMessage);
        this.key = key.getImmutable();
    }

    public CipherText toCipherText(@NonNull String encryptedMessage) {
        return new CipherText(getC1(), getC2(), getC3(), getAccessPolicy(), getOwnerId(), encryptedMessage);
    }
}
