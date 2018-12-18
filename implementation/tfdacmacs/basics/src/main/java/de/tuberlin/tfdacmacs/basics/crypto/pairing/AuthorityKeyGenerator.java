package de.tuberlin.tfdacmacs.basics.crypto.pairing;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.AuthorityKey;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class AuthorityKeyGenerator {

    /**
     * Generates the authority private and public key components.
     *
     * @param gpp the global public parameter
     * @return the private and public key pair
     */
    public AuthorityKey generate(@NonNull GlobalPublicParameter gpp) {
        Field Zr = gpp.getPairing().getZr();
        Element g = gpp.getG();

        Element x = Zr.newRandomElement();
        Element egg_x = gpp.getPairing().pairing(g, g).powZn(x);

        return new AuthorityKey(x, egg_x);
    }
}
