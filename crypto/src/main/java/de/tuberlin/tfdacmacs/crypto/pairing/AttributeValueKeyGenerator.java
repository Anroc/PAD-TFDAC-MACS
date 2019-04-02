package de.tuberlin.tfdacmacs.crypto.pairing;

import de.tuberlin.tfdacmacs.crypto.pairing.data.CipherText;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.VersionedID;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.*;
import de.tuberlin.tfdacmacs.crypto.pairing.util.HashGenerator;
import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AttributeValueKeyGenerator {

    private final HashGenerator hashGenerator;

    /**
     * Generates a new attribute private and public key.
     *
     * @param gpp the global public paramter
     * @return the attribute key
     */
    public AttributeValueKey generateNew(@NonNull GlobalPublicParameter gpp, @NonNull String attributeValueId) {
        return generate(gpp, attributeValueId, 0L);
    }

    /**
     * Generates a new attribute private and public key.
     *
     * @param gpp the global public paramter
     * @param currentAttributeValueKey the current attributeValueKey
     * @return the attribute key
     */
    public AttributeValueKey generateNext(@NonNull GlobalPublicParameter gpp, @NonNull AttributeValueKey currentAttributeValueKey) {
        return generate(gpp, currentAttributeValueKey.getAttributeValueId(), currentAttributeValueKey.getVersion() + 1);
    }

    private AttributeValueKey generate(GlobalPublicParameter gpp, String attributeValueId, long version) {
        Element g = gpp.getG();
        Element privateKey = gpp.zr().newRandomElement(); // y
        Element publicKey = g.powZn(privateKey); // g ** y

        return new AttributeValueKey(
                privateKey,
                publicKey,
                attributeValueId,
                version);
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
    public UserAttributeValueKey generateUserKey(
            @NonNull GlobalPublicParameter gpp,
            @NonNull String userId,
            @NonNull AuthorityKey.Private privateAuthorityKey,
            @NonNull AttributeValueKey.Private privateAttributeValueKey) {

        Element userIdHash = hashGenerator.g1Hash(gpp, userId);
        // g ** x * H(uid)**y
        return new UserAttributeValueKey(
                gpp.getG().powZn(privateAuthorityKey.getKey()).mul(userIdHash.powZn(privateAttributeValueKey.getKey())),
                privateAttributeValueKey.getVersion()
        );
    }

    /**
     * Computes a new key-update-key for the given userId.
     * The userId is assumed to have an valid attribute secret key already issued.
     * Returns a update key that needs to be distributed to the user.
     *
     * @param gpp Global public parameter
     * @param userId the user id that should receive the updated key
     * @param revokedAttributePrivateKey the private revoked attribute value key
     * @param newAttributePrivateKey the new attribute value key
     * @return a update key that needs to be distributed to the user
     */
    public UserAttributeValueUpdateKey generateUserUpdateKey(
            @NonNull GlobalPublicParameter gpp,
            @NonNull String userId,
            @NonNull AttributeValueKey.Private revokedAttributePrivateKey,
            @NonNull AttributeValueKey.Private newAttributePrivateKey) {
        Element userIdHash = hashGenerator.g1Hash(gpp, userId);
        // H(uid)**(y' - y)
        return new UserAttributeValueUpdateKey(
                userId,
                userIdHash.powZn(newAttributePrivateKey.getKey().duplicate().sub(revokedAttributePrivateKey.getKey())),
                revokedAttributePrivateKey.getVersion()
        );
    }

    /**
     * Generates a cipher text update key.
     * Only call with cipher text that have the revoked attribute in their policy.
     * Returns a cipher text update key that will be distributed to the server holding the cipher texts.
     *
     * @param cipherText the cipher text containing the revoked attribute
     * @param revokedAttributeValueKey the revoked attribute value key
     * @param newAttributeValueKey the new attribute value key
     * @param twoFactorPublicKey the two factory public key, may be null if not present
     * @return the cipher text update key
     */
    public CipherTextAttributeUpdateKey generateCipherTextUpdateKey(
            @NonNull CipherText cipherText,
            @NonNull AttributeValueKey revokedAttributeValueKey,
            @NonNull AttributeValueKey newAttributeValueKey,
            TwoFactorKey.Public twoFactorPublicKey) {
        // (g**s * g**alpha) **(y' - y)
        Element cuk = cipherText.getC2().duplicate();
        if(twoFactorPublicKey != null) {
            cuk = cuk.mul(twoFactorPublicKey.getKey());
        }

        cuk.powZn(newAttributeValueKey.getPrivateKey().getKey().duplicate()
                .sub(revokedAttributeValueKey.getPrivateKey().getKey()));

        return new CipherTextAttributeUpdateKey(
                cuk,
                newAttributeValueKey.getAttributeValueId(),
                Optional.ofNullable(twoFactorPublicKey).map(tf -> new VersionedID(cipherText.getOwnerId().getId(), tf.getVersion())).orElse(null),
                newAttributeValueKey.getPublicKey(),
                revokedAttributeValueKey.getVersion()
        );
    }
}
