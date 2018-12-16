package de.tuberlin.tfdacmacs.basics.crypto.pairing.data;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;

@Data
public class AttributeSecretComponents {

    private final Element userSecretAttributeKey;
    private final Element attributePublicKey;

    public AttributeSecretComponents(Element userSecretAttributeKey, Element attributePublicKey) {
        this.userSecretAttributeKey = userSecretAttributeKey.getImmutable();
        this.attributePublicKey = attributePublicKey.getImmutable();
    }
}
