package de.tuberlin.tfdacmacs.crypto.pairing;

import com.google.common.collect.Sets;
import de.tuberlin.tfdacmacs.crypto.pairing.aes.AESDecryptor;
import de.tuberlin.tfdacmacs.crypto.pairing.aes.AESEncryptor;
import de.tuberlin.tfdacmacs.crypto.pairing.data.*;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AuthorityKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.TwoFactorKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.UserAttributeValueKey;
import de.tuberlin.tfdacmacs.crypto.pairing.exceptions.AccessPolicyNotSatisfiedException;
import de.tuberlin.tfdacmacs.crypto.pairing.policy.AccessPolicyParser;
import de.tuberlin.tfdacmacs.crypto.pairing.util.HashGenerator;
import de.tuberlin.tfdacmacs.crypto.rsa.StringSymmetricCryptEngine;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import org.assertj.core.util.Maps;
import org.junit.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final TwoFactorKeyGenerator twoFactorKeyGenerator = new TwoFactorKeyGenerator(hashGenerator);

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
                pairing, pairingParameters, pairing.getG1().newRandomElement().getImmutable());
        return globalPublicParameter;
    }

    private AuthorityKey setupAuthority(GlobalPublicParameter gpp) {
        return authorityKeyGenerator.generate(gpp);
    }

    private AttributeValueKey createAttribute(GlobalPublicParameter gpp, String attributeValueIdentifier) {
        return attributeValueKeyGenerator.generateNew(gpp, attributeValueIdentifier);
    }

    private UserAttributeValueKey createUserAttributeKey(GlobalPublicParameter gpp, String userId, AuthorityKey authorityKey, AttributeValueKey attributeValueKey) {
        return attributeValueKeyGenerator.generateUserKey(gpp, userId, authorityKey.getPrivateKey(), attributeValueKey.getPrivateKey());
    }

    private TwoFactorKey generate2FA(GlobalPublicParameter gpp, TwoFactorKeyGenerator twoFactorKeyGenerator) {
        return twoFactorKeyGenerator.generateNew(gpp);
    }

    private DNFAccessPolicy parsePolicy(String policy, Map<String, AuthorityKey.Public> authorityPublicKeys, Set<AttributeValueKey.Public> attributePublicKeys) {
        return new AccessPolicyParser(
                (attributeValueId -> attributePublicKeys.stream().filter(attrPub -> attrPub.getAttributeValueId().equals(attributeValueId)).findAny().orElse(null)),
                (authorityId -> authorityPublicKeys.get(authorityId))
        ).parse(policy);
    }

    private DNFCipherText encrypt(GlobalPublicParameter gpp, DNFAccessPolicy dnfAccessPolicy, byte[] message) {
        return pairingCryptEngine.encrypt(message, dnfAccessPolicy, gpp, null);
    }

    private DNFCipherText encrypt(GlobalPublicParameter gpp, DNFAccessPolicy dnfAccessPolicy, byte[] message, DataOwner dataOwner) {
        return pairingCryptEngine.encrypt(message, dnfAccessPolicy, gpp, dataOwner);
    }

    private boolean addUserAttributeSecretComponent(String aid, AttributeValueKey attributeValueKey,
            UserAttributeValueKey userAttributeValueKey, Set<UserAttributeSecretComponent> userAttributeKeys) {
        return userAttributeKeys.add(new UserAttributeSecretComponent(userAttributeValueKey, attributeValueKey.getPublicKey(), aid));
    }

    private CipherText findSatisfyingCipherText(DNFCipherText cipherText,
            Set<UserAttributeSecretComponent> userAttributeKeys) {

        Set<VersionedID> attributeValueIds = userAttributeKeys.stream().map(UserAttributeSecretComponent::getAttributeValueId)
                .collect(Collectors.toSet());

        return cipherText.getCipherTexts().stream()
                .filter(ct -> attributeValueIds.containsAll(ct.getAccessPolicy()))
                .findFirst()
                .orElseThrow(
                        () -> new AccessPolicyNotSatisfiedException("Could not find satisfying access policy for user keys.")
                );
    }

    private byte[] decrypt(GlobalPublicParameter gpp, String uid, File file, CipherText cipherText,
            Set<UserAttributeSecretComponent> userAttributeKeys) {
        return pairingCryptEngine.decrypt(file.getData(), cipherText, gpp, uid, userAttributeKeys, null);
    }

    private byte[] decrypt(GlobalPublicParameter gpp, String uid, File file, CipherText cipherText,
            Set<UserAttributeSecretComponent> userAttributeKeys, TwoFactorKey.Secret twoFactorSecretKey) {
        return pairingCryptEngine.decrypt(file.getData(), cipherText, gpp, uid, userAttributeKeys, twoFactorSecretKey);
    }

    @Test
    public void demo() {
        GlobalPublicParameter gpp = setup();
        AuthorityKey authorityKey = setupAuthority(gpp);
        final String authorityId = "aa.tu-berlin.de";
        final String aidStudent = "aa.tu-berlin.de.role:Student";
        final String aidFaculty = "aa.tu-berlin.de.faculty:4";
        final String uid = "genesisUser@tu-berlin.de";
        final String strMessage = "No, Eve please :(";

        final String policy = String.format("(%s and %s)", aidStudent, aidFaculty);
        final byte[] message = strMessage.getBytes();

        AttributeValueKey studentAttributeValueKey = createAttribute(gpp, aidStudent);
        AttributeValueKey faculty4attributeValueKey = createAttribute(gpp, aidFaculty);

        UserAttributeValueKey studentUserAttributeValueKey = createUserAttributeKey(gpp, uid, authorityKey, studentAttributeValueKey);
        UserAttributeValueKey facultyUserAttributeValueKey = createUserAttributeKey(gpp, uid, authorityKey, faculty4attributeValueKey);

        DNFAccessPolicy dnfAccessPolicy = parsePolicy(
                policy,
                Maps.newHashMap(authorityId, authorityKey.getPublicKey()),
                Sets.newHashSet(studentAttributeValueKey.getPublicKey(), faculty4attributeValueKey.getPublicKey())
        );

        System.out.println(String.format("Encrypting message using policy: \t%s", policy));
        DNFCipherText dnfCipherText = encrypt(gpp, dnfAccessPolicy, message);
        System.out.println(String.format("Encrypted Message: \t%s", dnfCipherText.getFile().getData()));

        Set<UserAttributeSecretComponent> userAttributeKeys = new HashSet<>();
        addUserAttributeSecretComponent(aidStudent, studentAttributeValueKey, studentUserAttributeValueKey, userAttributeKeys);
        addUserAttributeSecretComponent(aidFaculty, faculty4attributeValueKey, facultyUserAttributeValueKey, userAttributeKeys);

        CipherText cipherText = findSatisfyingCipherText(dnfCipherText, userAttributeKeys);

        byte[] recoveredMessage = decrypt(gpp, uid, dnfCipherText.getFile(), cipherText, userAttributeKeys);
        String recoveredStrMessage = new String(recoveredMessage);

        System.out.println(String.format("Original Message: \t%s", strMessage));
        System.out.println(String.format("Recovered Message: \t%s", recoveredStrMessage));
    }

    @Test
    public void demo2FA() {
        GlobalPublicParameter gpp = setup();
        AuthorityKey authorityKey = setupAuthority(gpp);
        final String oid = "dataowner@tu-berlin.de";
        final String authorityId = "aa.tu-berlin.de";
        final String aidStudent = "aa.tu-berlin.de.role:Student";
        final String aidFaculty = "aa.tu-berlin.de.faculty:4";
        final String uid = "genesisUser@tu-berlin.de";
        final String strMessage = "No, Eve please :(";

        final String policy = String.format("(%s and %s)", aidStudent, aidFaculty);
        final byte[] message = strMessage.getBytes();

        AttributeValueKey studentAttributeValueKey = createAttribute(gpp, aidStudent);
        AttributeValueKey faculty4attributeValueKey = createAttribute(gpp, aidFaculty);

        UserAttributeValueKey studentUserAttributeValueKey = createUserAttributeKey(gpp, uid, authorityKey, studentAttributeValueKey);
        UserAttributeValueKey facultyUserAttributeValueKey = createUserAttributeKey(gpp, uid, authorityKey, faculty4attributeValueKey);

        DNFAccessPolicy dnfAccessPolicy = parsePolicy(
                policy,
                Maps.newHashMap(authorityId, authorityKey.getPublicKey()),
                Sets.newHashSet(studentAttributeValueKey.getPublicKey(), faculty4attributeValueKey.getPublicKey())
        );

        TwoFactorKey twoFactorKey = generate2FA(gpp, twoFactorKeyGenerator);
        DataOwner dataOwner = new DataOwner(oid, twoFactorKey.getPrivateKey());

        DNFCipherText dnfCipherText = encrypt(gpp, dnfAccessPolicy, message, dataOwner);
        System.out.println(String.format("Encrypted Message: \t%s", dnfCipherText.getFile().getData()));

        Set<UserAttributeSecretComponent> userAttributeKeys = new HashSet<>();
        addUserAttributeSecretComponent(aidStudent, studentAttributeValueKey, studentUserAttributeValueKey, userAttributeKeys);
        addUserAttributeSecretComponent(aidFaculty, faculty4attributeValueKey, facultyUserAttributeValueKey, userAttributeKeys);

        twoFactorKey = twoFactorKeyGenerator.generatePublicKeyForUser(gpp, twoFactorKey, uid);

        CipherText cipherText = findSatisfyingCipherText(dnfCipherText, userAttributeKeys);

        byte[] recoveredMessage = decrypt(gpp, uid, dnfCipherText.getFile(), cipherText, userAttributeKeys, twoFactorKey.getSecretKeyOfUser(uid));
        String recoveredStrMessage = new String(recoveredMessage);

        System.out.println(String.format("Original Message: \t%s", strMessage));
        System.out.println(String.format("Recovered Message: \t%s", recoveredStrMessage));
    }
}
