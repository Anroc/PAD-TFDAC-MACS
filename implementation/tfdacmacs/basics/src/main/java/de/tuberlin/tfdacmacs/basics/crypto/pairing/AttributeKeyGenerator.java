package de.tuberlin.tfdacmacs.basics.crypto.pairing;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.Key;
import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class AttributeKeyGenerator {

    private final MessageDigest messageDigest;

    public AttributeKeyGenerator() {
        try {
            this.messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] sha256Hash(@NonNull String input) {
        this.messageDigest.update(input.getBytes(StandardCharsets.UTF_8));
        return this.messageDigest.digest();
    }

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

        byte[] userIdBytes = sha256Hash(userId);
        Element userIdHash = gpp.getPairing().getG1().newElementFromHash(userIdBytes, 0, userIdBytes.length);
        // g ** x * H(uid)**y
        return gpp.getG().powZn(privateAuthorityKey).mul(userIdHash.powZn(privateAttributeValueKey));
    }
}
