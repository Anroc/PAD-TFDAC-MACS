package de.tuberlin.tfdacmacs.crypto.pairing.data;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.NonNull;

import java.util.Set;

@Data
public class CipherTextDescription extends CipherText {

    private final Element key;

    public CipherTextDescription(Element c1, Element c2, Element c3, Set<String> accessPolicy,
            String ownerId, Element key) {
        super(c1, c2, c3, accessPolicy, ownerId, null);
        this.key = key.getImmutable();
    }

    public CipherText bindTo(@NonNull File file) {
        return new CipherText(getC1(), getC2(), getC3(), getAccessPolicy(), getOwnerId(), file.getId());
    }
}
