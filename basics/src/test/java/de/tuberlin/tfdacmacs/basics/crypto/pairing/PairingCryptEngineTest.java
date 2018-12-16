package de.tuberlin.tfdacmacs.basics.crypto.pairing;

import de.tuberlin.tfdacmacs.basics.UnitTestSuite;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.*;
import it.unisa.dia.gas.jpbc.Element;
import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class PairingCryptEngineTest extends UnitTestSuite {

    private final byte[] message = "Hello World!".getBytes();
    private final String userId = UUID.randomUUID().toString();
    private GlobalPublicParameter gpp;
    private Key authorityKeys;
    private Key attributeKeys;
    private Element userSecretAttributeValueKey;
    private AndAccessPolicy andAccessPolicy;

    private String dataOwerId = UUID.randomUUID().toString();
    private Key twoFactoryKey;
    private DataOwner dataOwner;

    @Before
    public void setup() {
        gpp = gppTestFactory.create();
        authorityKeys = authorityKeyGenerator.generateAuthorityKey(gpp);
        attributeKeys = attributeKeyGenerator.generateAttributeValueKey(gpp);
        userSecretAttributeValueKey = attributeKeyGenerator
                .generateSecretUserKey(gpp, userId, authorityKeys.getPrivateKey(), attributeKeys.getPrivateKey());

        HashSet<AccessPolicyElement> accessPolicyElements = Sets.newHashSet();
        accessPolicyElements.add(new AccessPolicyElement(authorityKeys.getPublicKey(), attributeKeys.getPublicKey()));
        andAccessPolicy = new AndAccessPolicy(accessPolicyElements);

        twoFactoryKey = twoFactorKeyGenerator.generateTwoFactoryKey(gpp, userId);
        dataOwner = new DataOwner(dataOwerId, twoFactoryKey.getPrivateKey());
    }

    @Test
    public void encrypt_decrypt_passes_without_2FA() {
        // encrypt
        CipherText cipherText = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, null);
        assertThat(cipherText).hasNoNullFieldsOrPropertiesExcept("ownerId");

        // decrypt
        LinkedHashSet<AttributeSecretComponents> attributeSecretComponents = Sets.newLinkedHashSet(
                new AttributeSecretComponents(userSecretAttributeValueKey, attributeKeys.getPublicKey()));
        byte[] output = pairingCryptEngine.decrypt(cipherText, gpp, userId, attributeSecretComponents, null);

        assertSameElements(output, message);
    }

    @Test
    public void encrypt_decrypt_passes_without_2FA_sameEncryptionDoesNotResolveInSameCipherText() {
        // encrypt
        CipherText cipherText1 = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, null);
        CipherText cipherText2 = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, null);
        assertThat(cipherText1.getEncryptedMessage()).isNotEqualTo(cipherText2.getEncryptedMessage());
        assertThat(cipherText1.getC1()).isNotEqualTo(cipherText2.getC1());
        assertThat(cipherText1.getC2()).isNotEqualTo(cipherText2.getC2());
        assertThat(cipherText1.getC3()).isNotEqualTo(cipherText2.getC3());
    }

    @Test
    public void encrypt_decrypt_fails_without_2FA_with_multipleAttributes_sameAuthority() {
        Key attributeKeys2 = attributeKeyGenerator.generateAttributeValueKey(gpp);
        Element userAttributeSecretValueKey2 = attributeKeyGenerator
                .generateSecretUserKey(gpp, userId, authorityKeys.getPrivateKey(), attributeKeys2.getPrivateKey());

        HashSet<AccessPolicyElement> accessPolicyElements = Sets.newHashSet();
        accessPolicyElements.add(new AccessPolicyElement(authorityKeys.getPublicKey(), attributeKeys.getPublicKey()));
        accessPolicyElements.add(new AccessPolicyElement(authorityKeys.getPublicKey(), attributeKeys2.getPublicKey()));
        andAccessPolicy = new AndAccessPolicy(accessPolicyElements);

        // encrypt
        CipherText cipherText = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, null);
        assertThat(cipherText).hasNoNullFieldsOrPropertiesExcept("ownerId");

        // decrypt
        LinkedHashSet<AttributeSecretComponents> attributeSecretComponents = Sets.newLinkedHashSet(
                new AttributeSecretComponents(userSecretAttributeValueKey, attributeKeys.getPublicKey()),
                new AttributeSecretComponents(userAttributeSecretValueKey2, attributeKeys.getPublicKey()));
        byte[] output = pairingCryptEngine.decrypt(cipherText, gpp, userId, attributeSecretComponents, null);

        assertSameElements(output, message);
    }

    @Test
    public void encrypt_decrypt_fails_without_2FA_with_multipleAttributes_sameAuthority_butPolicyNotSatisfied() {
        Key attributeKeys2 = attributeKeyGenerator.generateAttributeValueKey(gpp);

        HashSet<AccessPolicyElement> accessPolicyElements = Sets.newHashSet();
        accessPolicyElements.add(new AccessPolicyElement(authorityKeys.getPublicKey(), attributeKeys.getPublicKey()));
        accessPolicyElements.add(new AccessPolicyElement(authorityKeys.getPublicKey(), attributeKeys2.getPublicKey()));
        andAccessPolicy = new AndAccessPolicy(accessPolicyElements);

        // encrypt
        CipherText cipherText = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, null);
        assertThat(cipherText).hasNoNullFieldsOrPropertiesExcept("ownerId");

        // decrypt
        LinkedHashSet<AttributeSecretComponents> attributeSecretComponents = Sets.newLinkedHashSet(
                new AttributeSecretComponents(userSecretAttributeValueKey, attributeKeys.getPublicKey()));
        byte[] output = pairingCryptEngine.decrypt(cipherText, gpp, userId, attributeSecretComponents, null);

        assertNotSameElements(output, message);
    }

    @Test
    public void encrypt_decrypt_fails_without_2FA_with_multipleAttributes_otherAuthorities_butPolicyNotSatisfied() {
        Key attributeKeys2 = attributeKeyGenerator.generateAttributeValueKey(gpp);
        Key authorityKey2 = authorityKeyGenerator.generateAuthorityKey(gpp);

        HashSet<AccessPolicyElement> accessPolicyElements = Sets.newHashSet();
        accessPolicyElements.add(new AccessPolicyElement(authorityKeys.getPublicKey(), attributeKeys.getPublicKey()));
        accessPolicyElements.add(new AccessPolicyElement(authorityKey2.getPublicKey(), attributeKeys2.getPublicKey()));
        andAccessPolicy = new AndAccessPolicy(accessPolicyElements);

        // encrypt
        CipherText cipherText = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, null);
        assertThat(cipherText).hasNoNullFieldsOrPropertiesExcept("ownerId");

        // decrypt
        LinkedHashSet<AttributeSecretComponents> attributeSecretComponents = Sets.newLinkedHashSet(
                new AttributeSecretComponents(userSecretAttributeValueKey, attributeKeys.getPublicKey()));
        byte[] output = pairingCryptEngine.decrypt(cipherText, gpp, userId, attributeSecretComponents, null);

        assertNotSameElements(output, message);
    }

    @Test
    public void encrypt_decrypt_passes_wit_FA() {
        // encrypt
        CipherText cipherText = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, dataOwner);
        assertThat(cipherText).hasNoNullFieldsOrProperties();

        // decrypt
        LinkedHashSet<AttributeSecretComponents> attributeSecretComponents = Sets.newLinkedHashSet(
                new AttributeSecretComponents(userSecretAttributeValueKey, attributeKeys.getPublicKey()));
        byte[] output = pairingCryptEngine.decrypt(cipherText, gpp, userId, attributeSecretComponents, twoFactoryKey.getPublicKey());

        assertSameElements(output, message);
    }

    @Test
    public void encrypt_decrypt_passes_with_FA_butUserHasNo2FAKey() {
        // encrypt
        CipherText cipherText = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, dataOwner);
        assertThat(cipherText).hasNoNullFieldsOrProperties();

        // decrypt
        LinkedHashSet<AttributeSecretComponents> attributeSecretComponents = Sets.newLinkedHashSet(
                new AttributeSecretComponents(userSecretAttributeValueKey, attributeKeys.getPublicKey()));
        byte[] output = pairingCryptEngine.decrypt(cipherText, gpp, userId, attributeSecretComponents, null);

        assertNotSameElements(output, message);
    }

    @Test
    public void abe_encrypt_decrypt() {
        // encrypt
        CipherTextDescription cipherText = pairingCryptEngine.abeEncrypt(andAccessPolicy, gpp, null);

        // decrypt
        LinkedHashSet<AttributeSecretComponents> attributeSecretComponents = Sets.newLinkedHashSet(
                new AttributeSecretComponents(userSecretAttributeValueKey, attributeKeys.getPublicKey()));
        Element key = pairingCryptEngine.abeDecrypt(cipherText, gpp, userId, attributeSecretComponents, null);

        assertSameElements(key.toBytes(), cipherText.getKey().toBytes());
        assertThat(key).isEqualTo(cipherText.getKey());
    }

    @Test
    public void abe_encrypt_decrypt_failsOnUnsatisfyingPolicy() {
        Key attributeKeys2 = attributeKeyGenerator.generateAttributeValueKey(gpp);
        Key authorityKey2 = authorityKeyGenerator.generateAuthorityKey(gpp);

        HashSet<AccessPolicyElement> accessPolicyElements = Sets.newHashSet();
        accessPolicyElements.add(new AccessPolicyElement(authorityKeys.getPublicKey(), attributeKeys.getPublicKey()));
        accessPolicyElements.add(new AccessPolicyElement(authorityKey2.getPublicKey(), attributeKeys2.getPublicKey()));
        andAccessPolicy = new AndAccessPolicy(accessPolicyElements);

        // encrypt
        CipherTextDescription cipherText = pairingCryptEngine.abeEncrypt(andAccessPolicy, gpp, null);

        // decrypt
        LinkedHashSet<AttributeSecretComponents> attributeSecretComponents = Sets.newLinkedHashSet(
                new AttributeSecretComponents(userSecretAttributeValueKey, attributeKeys.getPublicKey()));
        Element key = pairingCryptEngine.abeDecrypt(cipherText, gpp, userId, attributeSecretComponents, null);

        assertNotSameElements(key.toBytes(), cipherText.getKey().toBytes());
        assertThat(key).isNotEqualTo(cipherText.getKey());
    }
}