package de.tuberlin.tfdacmacs.basics.crypto.pairing.data;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class AttributeSecretComponents {

    private final Element userSecretAttributeKey;
    private final Element attributePublicKey;
}
