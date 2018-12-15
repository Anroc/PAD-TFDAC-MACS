package de.tuberlin.tfdacmacs.basics.crypto.pairing.data;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;

@Data
public class AccessPolicyElement {

    private final Element authorityPublicKey;
    private final Element attributePublicKey;

    public AccessPolicyElement(Element authorityPublicKey, Element attributePublicKey) {
        this.authorityPublicKey = authorityPublicKey.getImmutable();
        this.attributePublicKey = attributePublicKey.getImmutable();
    }


}
