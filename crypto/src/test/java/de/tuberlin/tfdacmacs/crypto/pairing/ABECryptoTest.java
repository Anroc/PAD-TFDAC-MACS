package de.tuberlin.tfdacmacs.crypto.pairing;

import de.tuberlin.tfdacmacs.crypto.UnitTestSuite;
import de.tuberlin.tfdacmacs.crypto.pairing.data.*;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.*;
import de.tuberlin.tfdacmacs.crypto.pairing.exceptions.AccessPolicyNotSatisfiedException;
import de.tuberlin.tfdacmacs.crypto.pairing.exceptions.VersionMismatchException;
import it.unisa.dia.gas.jpbc.Element;
import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

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
        attributeKeys = attributeValueKeyGenerator.generateNew(gpp, attributeValueIdentifier);
        userSecretAttributeValueKey = attributeValueKeyGenerator
                .generateUserKey(gpp, userId, authorityKeys.getPrivateKey(), attributeKeys.getPrivateKey());

        HashSet<AttributePolicyElement> attributePolicyElements = Sets.newHashSet();
        attributePolicyElements.add(new AttributePolicyElement(authorityKeys.getPublicKey(), attributeKeys.getPublicKey(), new VersionedID(attributeValueIdentifier, attributeKeys.getVersion())));
        andAccessPolicy = new AndAccessPolicy(attributePolicyElements);
    }

    @Test
    public void abe_encrypt_decrypt() {
        // encrypt
        CipherTextDescription cipherText = abeEncryptor.encrypt(andAccessPolicy, gpp, null, null);
        assertThat(cipherText.isTwoFactorSecured()).isFalse();

        // decrypt
        LinkedHashSet<UserAttributeSecretComponent> userAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponent(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier));
        Element key = abeDecryptor.decrypt(cipherText, gpp, userId, userAttributeSecretComponents, null);

        assertSameElements(key.toBytes(), cipherText.getKey().toBytes());
        assertThat(key).isEqualTo(cipherText.getKey());
    }

    @Test
    public void abe_encrypt_decrypt_failsOnUnsatisfyingPolicy() {
        String attrValueId = "aa.hpi.de.role:student";
        AttributeValueKey attributeKeys2 = attributeValueKeyGenerator.generateNew(gpp, attrValueId);
        AuthorityKey authorityAuthorityKey2 = authorityKeyGenerator.generate(gpp);

        andAccessPolicy.getAttributePolicyElements().add(
                new AttributePolicyElement(authorityAuthorityKey2.getPublicKey(), attributeKeys2.getPublicKey(), new VersionedID(attrValueId, attributeKeys2.getVersion()))
        );

        // encrypt
        CipherTextDescription cipherText = abeEncryptor.encrypt(andAccessPolicy, gpp, null, null);
        assertThat(cipherText.isTwoFactorSecured()).isFalse();

        // decrypt
        LinkedHashSet<UserAttributeSecretComponent> userAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponent(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier));
        assertThatExceptionOfType(AccessPolicyNotSatisfiedException.class).isThrownBy(
                () -> abeDecryptor.decrypt(cipherText, gpp, userId, userAttributeSecretComponents, null)
        );
    }

    @Test
    public void ciphertext_update_passes() {
        // encrypt
        CipherTextDescription cipherText = abeEncryptor.encrypt(andAccessPolicy, gpp, null, null);
        assertThat(cipherText.isTwoFactorSecured()).isFalse();

        // generate update components
        AttributeValueKey newAttributeValueKey = attributeValueKeyGenerator
                .generateNext(gpp, attributeKeys);
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
        LinkedHashSet<UserAttributeSecretComponent> userAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponent(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier));
        assertThatExceptionOfType(VersionMismatchException.class).isThrownBy(
                () -> abeDecryptor.decrypt(updatedCipherText, gpp, userId, userAttributeSecretComponents, null)
        );

        // updated components are able to decrypt
        LinkedHashSet<UserAttributeSecretComponent> updatedUserAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponent(updateUserSecretKey, newAttributeValueKey.getPublicKey(), attributeValueIdentifier));
        Element key = abeDecryptor.decrypt(updatedCipherText, gpp, userId, updatedUserAttributeSecretComponents, null);
        assertSameElements(key.toBytes(), cipherText.getKey().toBytes());
    }

    @Test
    public void ciphertext_update_passes_withMultipleAttributePublicy() {
        // encrypt
        String attrValueId = "aa.hpi.de.role:student";
        AttributeValueKey attributeKeys2 = attributeValueKeyGenerator.generateNew(gpp, attrValueId);
        AuthorityKey authorityAuthorityKey2 = authorityKeyGenerator.generate(gpp);

        andAccessPolicy.getAttributePolicyElements().add(
                new AttributePolicyElement(authorityAuthorityKey2.getPublicKey(), attributeKeys2.getPublicKey(), new VersionedID(attrValueId, attributeKeys2.getVersion()))
        );
        CipherTextDescription cipherText = abeEncryptor.encrypt(andAccessPolicy, gpp, null, null);
        assertThat(cipherText.isTwoFactorSecured()).isFalse();

        UserAttributeValueKey userSecretAttributeValueKey2 = attributeValueKeyGenerator
                .generateUserKey(gpp, userId, authorityAuthorityKey2.getPrivateKey(),
                        attributeKeys2.getPrivateKey());

        // generate update components
        AttributeValueKey newAttributeValueKey = attributeValueKeyGenerator
                .generateNext(gpp, attributeKeys);
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
        LinkedHashSet<UserAttributeSecretComponent> userAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponent(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier),
                new UserAttributeSecretComponent(userSecretAttributeValueKey2, attributeKeys2.getPublicKey(), attrValueId)
        );
        assertThatExceptionOfType(VersionMismatchException.class).isThrownBy(
                () -> abeDecryptor.decrypt(updatedCipherText, gpp, userId, userAttributeSecretComponents, null)
        );

        // updated components are able to decrypt
        LinkedHashSet<UserAttributeSecretComponent> updatedUserAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponent(updateUserSecretKey, newAttributeValueKey.getPublicKey(), attributeValueIdentifier),
                new UserAttributeSecretComponent(userSecretAttributeValueKey2, attributeKeys2.getPublicKey(), attrValueId)
        );
        Element key = abeDecryptor.decrypt(updatedCipherText, gpp, userId, updatedUserAttributeSecretComponents, null);
        assertSameElements(key.toBytes(), cipherText.getKey().toBytes());
    }

    @Test
    public void ciphertext_2fa_update_passes() {
        String ownerId = UUID.randomUUID().toString();
        TwoFactorKey twoFactorKey = twoFactorKeyGenerator.generateNew(gpp, userId);
        TwoFactorKey.Public user2FAKey = twoFactorKey.getPublicKeyOfUser(userId);

        // encrypt
        CipherTextDescription cipherText = abeEncryptor.encrypt(andAccessPolicy, gpp, new DataOwner(ownerId, twoFactorKey.getPrivateKey()), null);
        assertThat(cipherText.isTwoFactorSecured()).isTrue();

        // generate Update components
        TwoFactorKey newTwoFactorKey = twoFactorKeyGenerator.generateNext(gpp, twoFactorKey, userId);
        TwoFactorUpdateKey twoFactorUpdateKey = twoFactorKeyGenerator.generateUpdateKey(gpp, twoFactorKey.getPrivateKey(), newTwoFactorKey.getPrivateKey(), userId);
        CipherText2FAUpdateKey cipherText2FAUpdateKey = twoFactorKeyGenerator.generateCipherTextUpdateKey(twoFactorKey.getPrivateKey(), newTwoFactorKey.getPrivateKey(), attributeKeys.getPublicKey(), ownerId);

        // update components
        TwoFactorKey.Public newUser2FAKey = user2FAKey.update(twoFactorUpdateKey);
        CipherText updatedCipherText = abeEncryptor
                .update(gpp, cipherText, andAccessPolicy, Sets.newLinkedHashSet(cipherText2FAUpdateKey));

        // assert old keys are not legitimit anymore
        LinkedHashSet<UserAttributeSecretComponent> userAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponent(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier)
        );
        assertThatExceptionOfType(VersionMismatchException.class).isThrownBy(
                () -> abeDecryptor.decrypt(updatedCipherText, gpp, userId, userAttributeSecretComponents, twoFactorKey.getPublicKeyOfUser(userId))
        );

        // assert update succeedes
        LinkedHashSet<UserAttributeSecretComponent> updateUserAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponent(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier)
        );
        Element message2 = abeDecryptor
                .decrypt(updatedCipherText, gpp, userId, updateUserAttributeSecretComponents, newUser2FAKey);
        assertSameElements(message2.toBytes(), cipherText.getKey().toBytes());
    }

}
