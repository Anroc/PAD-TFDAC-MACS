package de.tuberlin.tfdacmacs.basics.crypto.pairing;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.AndAccessPolicy;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.CipherText;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.DataOwner;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.GlobalPublicParameter;
import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Map;
import java.util.Set;

@Component
public class CryptEngine {

    public CipherText encrypt(
            byte[] data,
            @NonNull AndAccessPolicy andAccessPolicy,
            @NonNull GlobalPublicParameter gpp,
            DataOwner dataOwner) {
        Element message = gpp.getG().mul(new BigInteger(data));
        Element s = gpp.getPairing().getZr().newRandomElement();

        Map<Element, Set<Element>> policy = andAccessPolicy.groupByAttributeAuthority();

        Element c1 = null;
        Element c2 = gpp.getG().powZn(s);
        Element c3 = null;
        for(Map.Entry<Element, Set<Element>> entry : policy.entrySet()) {
            Element authorityPublicKey = entry.getKey().duplicate();
            int n = entry.getValue().size();

            c1 = mulOrDefault(c1, authorityPublicKey.pow(BigInteger.valueOf(n)));

            for( Element attributePublicKey : entry.getValue()) {
                c3 = mulOrDefault(c3, attributePublicKey);
            }
        }

        c1 = message.duplicate().mul(c1.powZn(s));

        if(dataOwner == null) {
            c3.powZn(s);
            return new CipherText(c1, c2, c3, null);
        } else {
            c3.powZn(s.duplicate().add(dataOwner.getTwoFactorPrivateKey()));
            return new CipherText(c1, c2, c3, null, dataOwner.getId());
        }
    }

    private Element mulOrDefault(Element target, @NonNull Element multiplier) {
        if(target == null) {
            return multiplier;
        } else {
            return target.mul(multiplier);
        }
    }
}
