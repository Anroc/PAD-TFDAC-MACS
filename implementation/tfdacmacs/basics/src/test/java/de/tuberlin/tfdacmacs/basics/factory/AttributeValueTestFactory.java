package de.tuberlin.tfdacmacs.basics.factory;

import de.tuberlin.tfdacmacs.basics.attributes.data.AttributeValue;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.PairingGenerator;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.AttributeValueKey;
import it.unisa.dia.gas.jpbc.Pairing;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttributeValueTestFactory {

    private final PairingGenerator pairingGenerator;

    public AttributeValue createString() {
        Pairing pairing = pairingGenerator.setupPairing();

        return new AttributeValue(
                "testValue",
                new AttributeValueKey(
                    pairing.getZr().newRandomElement(),
                    pairing.getG1().newRandomElement(),
                        "test.value:testValue"
                )
        );
    }
}
