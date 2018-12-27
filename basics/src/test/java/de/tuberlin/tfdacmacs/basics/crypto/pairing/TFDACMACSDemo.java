package de.tuberlin.tfdacmacs.basics.crypto.pairing;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.aes.AESDecryptor;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.aes.AESEncryptor;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.*;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.AttributeValueKey;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.AuthorityKey;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.UserAttributeValueKey;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.util.HashGenerator;
import de.tuberlin.tfdacmacs.basics.crypto.rsa.StringSymmetricCryptEngine;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class TFDACMACSDemo {

    private final StringSymmetricCryptEngine symmetricCryptEngine = new StringSymmetricCryptEngine();
    private final HashGenerator hashGenerator = new HashGenerator();

    private final AESEncryptor aesEncryptor = new AESEncryptor(hashGenerator, symmetricCryptEngine);
    private final AESDecryptor aesDecryptor = new AESDecryptor(hashGenerator, symmetricCryptEngine);
    private final ABEEncryptor abeEncryptor = new ABEEncryptor();
    private final ABEDecryptor abeDecryptor = new ABEDecryptor(hashGenerator);

    private final PairingCryptEngine pairingCryptEngine = new PairingCryptEngine(
            aesEncryptor,
            aesDecryptor,
            abeEncryptor,
            abeDecryptor
    );

    private final AuthorityKeyGenerator authorityKeyGenerator = new AuthorityKeyGenerator();
    private final AttributeValueKeyGenerator attributeValueKeyGenerator = new AttributeValueKeyGenerator(hashGenerator);

    /**
     * Setup the pairing and calculate a new pairing type A curve.
     *
     * @return the global public parameter
     */
    private GlobalPublicParameter setup() {
        PairingGenerator pairingGenerator = new PairingGenerator();
        PairingParameters pairingParameters = pairingGenerator.generateNewTypeACurveParameter();
        Pairing pairing = pairingGenerator.setupPairing(pairingParameters);
        GlobalPublicParameter globalPublicParameter = new GlobalPublicParameter(
                pairing, pairingParameters, pairing.getG1().newRandomElement().getImmutable(), null);
        return globalPublicParameter;
    }

    private AuthorityKey setupAuthority(GlobalPublicParameter gpp) {
        return authorityKeyGenerator.generate(gpp);
    }

    private AttributeValueKey createAttribute(GlobalPublicParameter gpp, String attributeValueIdentifier) {
        return attributeValueKeyGenerator.generate(gpp, attributeValueIdentifier);
    }

    private UserAttributeValueKey createUserAttributeKey(GlobalPublicParameter gpp, String userId, AuthorityKey authorityKey, AttributeValueKey attributeValueKey) {
        return attributeValueKeyGenerator.generateUserKey(gpp, userId, authorityKey.getPrivateKey(), attributeValueKey.getPrivateKey());
    }

    private void addPolicyElement(AuthorityKey authorityKey, String aid, AttributeValueKey attributeValueKey,
            Set<AccessPolicyElement> policy) {
        policy.add(new AccessPolicyElement(authorityKey.getPublicKey(), attributeValueKey.getPublicKey(), aid));
    }

    private CipherText encrypt(GlobalPublicParameter gpp, AndAccessPolicy andAccessPolicy, byte[] message) {
        return pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, null);
    }

    private boolean addUserAttributeSecretComponent(String aid, AttributeValueKey attributeValueKey,
            UserAttributeValueKey userAttributeValueKey, Set<UserAttributeSecretComponents> userAttributeKeys) {
        return userAttributeKeys.add(new UserAttributeSecretComponents(userAttributeValueKey, attributeValueKey.getPublicKey(), aid));
    }

    private byte[] decrypt(GlobalPublicParameter gpp, String uid, CipherText cipherText,
            Set<UserAttributeSecretComponents> userAttributeKeys) {
        return pairingCryptEngine.decrypt(cipherText, gpp, uid, userAttributeKeys, null);
    }

    @Test
    public void demo() {
        GlobalPublicParameter gpp = setup();
        AuthorityKey authorityKey = setupAuthority(gpp);
        final String aid = "aa.tu-berlin.de.role:Student";
        final String uid = "genesisUser@tu-berlin.de";
        final String strMessage = "No, Eve please :(";
        final byte[] message = strMessage.getBytes();

        AttributeValueKey attributeValueKey = createAttribute(gpp, aid);

        UserAttributeValueKey userAttributeValueKey = createUserAttributeKey(gpp, uid, authorityKey, attributeValueKey);

        Set<AccessPolicyElement> policy = new HashSet<>();
        addPolicyElement(authorityKey, aid, attributeValueKey, policy);
        AndAccessPolicy andAccessPolicy = new AndAccessPolicy(policy);

        CipherText cipherText = encrypt(gpp, andAccessPolicy, message);

        Set<UserAttributeSecretComponents> userAttributeKeys = new HashSet<>();
        addUserAttributeSecretComponent(aid, attributeValueKey, userAttributeValueKey, userAttributeKeys);

        byte[] recoveredMessage = decrypt(gpp, uid, cipherText, userAttributeKeys);
        String recoveredStrMessage = new String(recoveredMessage);

        System.out.println(String.format("Original Message: \t%s", strMessage));
        System.out.println(String.format("Recovered Message: \t%s", recoveredStrMessage));
    }


}
