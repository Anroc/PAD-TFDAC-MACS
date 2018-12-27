package de.tuberlin.tfdacmacs.basics.crypto.pairing;

import de.tuberlin.tfdacmacs.basics.UnitTestSuite;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.*;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.*;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.exceptions.AccessPolicyNotSatisfiedException;
import it.unisa.dia.gas.jpbc.Element;
import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ABECryptoTest extends UnitTestSuite {

    private final String userId = UUID.randomUUID().toString();
    private GlobalPublicParameter gpp;
    private AuthorityKey authorityKeys;
    private AttributeValueKey attributeKeys;
    private String attributeValueIdentifier = "aa.tu-berlin.de.role:professor";
    private UserAttributeValueKey userSecretAttributeValueKey;
    private AndAccessPolicy andAccessPolicy;

    @Before
    public void setup() {
        gpp = gppTestFactory.create();
        authorityKeys = authorityKeyGenerator.generate(gpp);
        attributeKeys = attributeValueKeyGenerator.generate(gpp, attributeValueIdentifier);
        userSecretAttributeValueKey = attributeValueKeyGenerator
                .generateUserKey(gpp, userId, authorityKeys.getPrivateKey(), attributeKeys.getPrivateKey());

        HashSet<AccessPolicyElement> accessPolicyElements = Sets.newHashSet();
        accessPolicyElements.add(new AccessPolicyElement(authorityKeys.getPublicKey(), attributeKeys.getPublicKey(), attributeValueIdentifier));
        andAccessPolicy = new AndAccessPolicy(accessPolicyElements);
    }

    @Test
    public void abe_encrypt_decrypt() {
        // encrypt
        CipherTextDescription cipherText = abeEncryptor.encrypt(andAccessPolicy, gpp, null);
        assertThat(cipherText.isTwoFactorSecured()).isFalse();

        // decrypt
        LinkedHashSet<UserAttributeSecretComponents> userAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponents(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier));
        Element key = abeDecryptor.decrypt(cipherText, gpp, userId, userAttributeSecretComponents, null);

        assertSameElements(key.toBytes(), cipherText.getKey().toBytes());
        assertThat(key).isEqualTo(cipherText.getKey());
    }

    @Test
    public void abe_encrypt_decrypt_failsOnUnsatisfyingPolicy() {
        String attrValueId = "aa.hpi.de.role:student";
        AttributeValueKey attributeKeys2 = attributeValueKeyGenerator.generate(gpp, attrValueId);
        AuthorityKey authorityAuthorityKey2 = authorityKeyGenerator.generate(gpp);

        andAccessPolicy.getAccessPolicyElements().add(
                new AccessPolicyElement(authorityAuthorityKey2.getPublicKey(), attributeKeys2.getPublicKey(), attrValueId)
        );

        // encrypt
        CipherTextDescription cipherText = abeEncryptor.encrypt(andAccessPolicy, gpp, null);
        assertThat(cipherText.isTwoFactorSecured()).isFalse();

        // decrypt
        LinkedHashSet<UserAttributeSecretComponents> userAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponents(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier));
        assertThatExceptionOfType(AccessPolicyNotSatisfiedException.class).isThrownBy(
                () -> pairingCryptEngine.decrypt(cipherText, gpp, userId, userAttributeSecretComponents, null)
        );
    }

    @Test
    public void ciphertext_update_passes() {
        // encrypt
        CipherTextDescription cipherText = abeEncryptor.encrypt(andAccessPolicy, gpp, null);
        assertThat(cipherText.isTwoFactorSecured()).isFalse();

        // generate update components
        AttributeValueKey newAttributeValueKey = attributeValueKeyGenerator
                .generate(gpp, attributeValueIdentifier);
        UserAttributeValueUpdateKey newUserAttributeValueUpdateKey = attributeValueKeyGenerator
                .generateUserUpdateKey(gpp, userId, attributeKeys.getPrivateKey(),
                        newAttributeValueKey.getPrivateKey());
        CipherTextAttributeUpdateKey cipherTextAttributeUpdateKey = attributeValueKeyGenerator
                .generateCipherTextUpdateKey(cipherText, attributeKeys, newAttributeValueKey, null);

        // 1. update user key
        UserAttributeValueKey updateUserSecretKey = userSecretAttributeValueKey.update(newUserAttributeValueUpdateKey);

        // 2. update cipher text
        CipherText updatedCipherText = abeEncryptor.update(gpp, cipherText, andAccessPolicy,
                cipherTextAttributeUpdateKey);

        // oldUserSecret component is not able to decrypt new ciphertext
        LinkedHashSet<UserAttributeSecretComponents> userAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponents(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier));
        Element key = abeDecryptor.decrypt(updatedCipherText, gpp, userId, userAttributeSecretComponents, null);
        assertNotSameElements(key.toBytes(), cipherText.getKey().toBytes());

        // updated components are able to decrypt
        LinkedHashSet<UserAttributeSecretComponents> updatedUserAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponents(updateUserSecretKey, newAttributeValueKey.getPublicKey(), attributeValueIdentifier));
        key = abeDecryptor.decrypt(updatedCipherText, gpp, userId, updatedUserAttributeSecretComponents, null);
        assertSameElements(key.toBytes(), cipherText.getKey().toBytes());
    }

    @Test
    public void ciphertext_update_passes_withMultipleAttributePublicy() {
        // encrypt
        String attrValueId = "aa.hpi.de.role:student";
        AttributeValueKey attributeKeys2 = attributeValueKeyGenerator.generate(gpp, attrValueId);
        AuthorityKey authorityAuthorityKey2 = authorityKeyGenerator.generate(gpp);

        andAccessPolicy.getAccessPolicyElements().add(
                new AccessPolicyElement(authorityAuthorityKey2.getPublicKey(), attributeKeys2.getPublicKey(), attrValueId)
        );
        CipherTextDescription cipherText = abeEncryptor.encrypt(andAccessPolicy, gpp, null);
        assertThat(cipherText.isTwoFactorSecured()).isFalse();

        UserAttributeValueKey userSecretAttributeValueKey2 = attributeValueKeyGenerator
                .generateUserKey(gpp, userId, authorityAuthorityKey2.getPrivateKey(),
                        attributeKeys2.getPrivateKey());

        // generate update components
        AttributeValueKey newAttributeValueKey = attributeValueKeyGenerator
                .generate(gpp, attributeValueIdentifier);
        UserAttributeValueUpdateKey newUserAttributeValueUpdateKey = attributeValueKeyGenerator
                .generateUserUpdateKey(gpp, userId, attributeKeys.getPrivateKey(),
                        newAttributeValueKey.getPrivateKey());
        CipherTextAttributeUpdateKey cipherTextAttributeUpdateKey = attributeValueKeyGenerator
                .generateCipherTextUpdateKey(cipherText, attributeKeys, newAttributeValueKey, null);

        // 1. update user key
        UserAttributeValueKey updateUserSecretKey = userSecretAttributeValueKey.update(newUserAttributeValueUpdateKey);

        // 2. update cipher text
        CipherText updatedCipherText = abeEncryptor.update(gpp, cipherText, andAccessPolicy,
                cipherTextAttributeUpdateKey);

        // oldUserSecret component is not able to decrypt new ciphertext
        LinkedHashSet<UserAttributeSecretComponents> userAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponents(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier),
                new UserAttributeSecretComponents(userSecretAttributeValueKey2, attributeKeys2.getPublicKey(), attrValueId)
        );
        Element key = abeDecryptor.decrypt(updatedCipherText, gpp, userId, userAttributeSecretComponents, null);
        assertNotSameElements(key.toBytes(), cipherText.getKey().toBytes());

        // updated components are able to decrypt
        LinkedHashSet<UserAttributeSecretComponents> updatedUserAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponents(updateUserSecretKey, newAttributeValueKey.getPublicKey(), attributeValueIdentifier),
                new UserAttributeSecretComponents(userSecretAttributeValueKey2, attributeKeys2.getPublicKey(), attrValueId)
        );
        key = abeDecryptor.decrypt(updatedCipherText, gpp, userId, updatedUserAttributeSecretComponents, null);
        assertSameElements(key.toBytes(), cipherText.getKey().toBytes());
    }

    @Test
    public void ciphertext_2fa_update_passes() {
        String ownerId = UUID.randomUUID().toString();
        TwoFactorKey twoFactorKey = twoFactorKeyGenerator.generate(gpp, userId);
        TwoFactorKey.Public user2FAKey = twoFactorKey.getPublicKey();

        // encrypt
        CipherTextDescription cipherText = abeEncryptor.encrypt(andAccessPolicy, gpp, new DataOwner(ownerId, twoFactorKey.getPrivateKey()));
        assertThat(cipherText.isTwoFactorSecured()).isTrue();

        // generate Update components
        TwoFactorKey newTwoFactorKey = twoFactorKeyGenerator.generate(gpp, userId);
        TwoFactorUpdateKey twoFactorUpdateKey = twoFactorKeyGenerator.generateUpdateKey(gpp, twoFactorKey.getPrivateKey(), newTwoFactorKey.getPrivateKey(), userId);
        CipherText2FAUpdateKey cipherText2FAUpdateKey = twoFactorKeyGenerator.generateCipherTextUpdateKey(twoFactorKey.getPrivateKey(), newTwoFactorKey.getPrivateKey(), attributeKeys.getPublicKey(), ownerId);

        // update components
        TwoFactorKey.Public newUser2FAKey = user2FAKey.update(twoFactorUpdateKey);
        CipherText updatedCipherText = abeEncryptor
                .update(gpp, cipherText, andAccessPolicy, Sets.newLinkedHashSet(cipherText2FAUpdateKey));

        // assert old keys are not legitimit anymore
        LinkedHashSet<UserAttributeSecretComponents> userAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponents(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier)
        );
        Element message = abeDecryptor
                .decrypt(updatedCipherText, gpp, userId, userAttributeSecretComponents, twoFactorKey.getPublicKey());
        assertNotSameElements(message.toBytes(), cipherText.getKey().toBytes());

        // assert update succeedes
        LinkedHashSet<UserAttributeSecretComponents> updateUserAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponents(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier)
        );
        Element message2 = abeDecryptor
                .decrypt(updatedCipherText, gpp, userId, updateUserAttributeSecretComponents, newUser2FAKey);
        assertSameElements(message2.toBytes(), cipherText.getKey().toBytes());
    }

}
