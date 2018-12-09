package de.tuberlin.tfdacmacs.basics.crypto;

import de.tuberlin.tfdacmacs.basics.attributes.data.AttributeValue;
import de.tuberlin.tfdacmacs.basics.gpp.data.GlobalPublicParameter;
import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class AttributeKeyGenerator {

    /**
     * Generates new attribute private and public keys for the given attribute value.
     *
     * @param value the attribute value
     * @param gpp the global public parameter
     * @param <T> the type of the attribute
     * @return the computed {@link AttributeValue}
     */
    public <T> AttributeValue<T> generateAttributeKeys(@NonNull T value, @NonNull GlobalPublicParameter gpp) {
        Element g = gpp.getG();
        Element privateKey = gpp.getPairing().getZr().newRandomElement();
        Element publicKey = g.powZn(privateKey);

        return new AttributeValue(privateKey, publicKey, value);
    }
}
