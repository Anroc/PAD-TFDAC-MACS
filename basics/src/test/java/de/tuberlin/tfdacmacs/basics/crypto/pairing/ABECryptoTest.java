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
        authorityKeys = authorityKeyGenerator.generateAuthorityKey(gpp);
        attributeKeys = attributeKeyManager.generateAttributeValueKey(gpp, attributeValueIdentifier);
        userSecretAttributeValueKey = attributeKeyManager
                .generateSecretUserKey(gpp, userId, authorityKeys.getPrivateKey(), attributeKeys.getPrivateKey());

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
        LinkedHashSet<AttributeSecretComponents> attributeSecretComponents = Sets.newLinkedHashSet(
                new AttributeSecretComponents(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier));
        Element key = abeDecryptor.decrypt(cipherText, gpp, userId, attributeSecretComponents, null);

        assertSameElements(key.toBytes(), cipherText.getKey().toBytes());
        assertThat(key).isEqualTo(cipherText.getKey());
    }

    @Test
    public void abe_encrypt_decrypt_failsOnUnsatisfyingPolicy() {
        String attrValueId = "aa.hpi.de.role:student";
        AttributeValueKey attributeKeys2 = attributeKeyManager.generateAttributeValueKey(gpp, attrValueId);
        AuthorityKey authorityAuthorityKey2 = authorityKeyGenerator.generateAuthorityKey(gpp);

        andAccessPolicy.getAccessPolicyElements().add(
                new AccessPolicyElement(authorityAuthorityKey2.getPublicKey(), attributeKeys2.getPublicKey(), attrValueId)
        );

        // encrypt
        CipherTextDescription cipherText = abeEncryptor.encrypt(andAccessPolicy, gpp, null);
        assertThat(cipherText.isTwoFactorSecured()).isFalse();

        // decrypt
        LinkedHashSet<AttributeSecretComponents> attributeSecretComponents = Sets.newLinkedHashSet(
                new AttributeSecretComponents(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier));
        assertThatExceptionOfType(AccessPolicyNotSatisfiedException.class).isThrownBy(
                () -> pairingCryptEngine.decrypt(cipherText, gpp, userId, attributeSecretComponents, null)
        );
    }

    @Test
    public void ciphertext_update_passes() {
        // encrypt
        CipherTextDescription cipherText = abeEncryptor.encrypt(andAccessPolicy, gpp, null);
        assertThat(cipherText.isTwoFactorSecured()).isFalse();

        // generate update components
        AttributeValueKey newAttributeValueKey = attributeKeyManager
                .generateAttributeValueKey(gpp, attributeValueIdentifier);
        UserAttributeValueUpdateKey newUserAttributeValueUpdateKey = attributeKeyManager
                .generateSecetUserUpdateKey(gpp, userId, attributeKeys.getPrivateKey(),
                        newAttributeValueKey.getPrivateKey());
        CipherTextUpdateKey cipherTextUpdateKey = attributeKeyManager.generateCipherTextUpdateKey(cipherText, attributeKeys, newAttributeValueKey, null);

        // 1. update user key
        UserAttributeValueKey updateUserSecretKey = userSecretAttributeValueKey.update(newUserAttributeValueUpdateKey);

        // 2. update cipher text
        CipherText updatedCipherText = abeEncryptor.update(gpp, cipherText, andAccessPolicy, cipherTextUpdateKey);

        // oldUserSecret component is not able to decrypt new ciphertext
        LinkedHashSet<AttributeSecretComponents> attributeSecretComponents = Sets.newLinkedHashSet(
                new AttributeSecretComponents(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier));
        Element key = abeDecryptor.decrypt(updatedCipherText, gpp, userId, attributeSecretComponents, null);
        assertNotSameElements(key.toBytes(), cipherText.getKey().toBytes());

        // updated components are able to decrypt
        LinkedHashSet<AttributeSecretComponents> updatedAttributeSecretComponents = Sets.newLinkedHashSet(
                new AttributeSecretComponents(updateUserSecretKey, newAttributeValueKey.getPublicKey(), attributeValueIdentifier));
        key = abeDecryptor.decrypt(updatedCipherText, gpp, userId, updatedAttributeSecretComponents, null);
        assertSameElements(key.toBytes(), cipherText.getKey().toBytes());


    }

}
