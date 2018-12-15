package de.tuberlin.tfdacmacs.basics.crypto.pairing;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.Key;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.util.HashGenerator;
import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttributeKeyGenerator {

    private final HashGenerator hashGenerator;

    /**
     * Generates a new attribute private and public key.
     *
     * @param gpp the global public paramter
     * @return the attribute key
     */
    public Key generateAttributeValueKey(@NonNull GlobalPublicParameter gpp) {
        Element g = gpp.getG();
        Element privateKey = gpp.getPairing().getZr().newRandomElement(); // y
        Element publicKey = g.powZn(privateKey); // g ** y

        return new Key(privateKey, publicKey);
    }

    /**
     * Generates new user secret keys from the given parameter.
     *
     * @param gpp the global public paramter
     * @param userId the user id
     * @param privateAuthorityKey the private authority key
     * @param privateAttributeValueKey the private attribute value key
     * @return users secret key
     */
    public Element generateSecretUserKey(
            @NonNull GlobalPublicParameter gpp,
            @NonNull String userId,
            @NonNull Element privateAuthorityKey,
            @NonNull Element privateAttributeValueKey) {

        Element userIdHash = hashGenerator.g1Hash(gpp, userId);
        // g ** x * H(uid)**y
        return gpp.getG().powZn(privateAuthorityKey).mul(userIdHash.powZn(privateAttributeValueKey));
    }
}
