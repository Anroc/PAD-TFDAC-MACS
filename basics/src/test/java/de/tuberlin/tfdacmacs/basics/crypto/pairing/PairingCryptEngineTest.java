package de.tuberlin.tfdacmacs.basics.crypto.pairing;

import de.tuberlin.tfdacmacs.basics.UnitTestSuite;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.*;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.AttributeValueKey;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.AuthorityKey;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.TwoFactorKey;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.UserAttributeValueKey;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.exceptions.AccessPolicyNotSatisfiedException;
import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class PairingCryptEngineTest extends UnitTestSuite {

    private final byte[] message = "Hello World!".getBytes();
    private final String userId = UUID.randomUUID().toString();
    private GlobalPublicParameter gpp;
    private AuthorityKey authorityKeys;
    private AttributeValueKey attributeKeys;
    private String attributeValueIdentifier = "aa.tu-berlin.de.role:professor";
    private UserAttributeValueKey userSecretAttributeValueKey;
    private AndAccessPolicy andAccessPolicy;

    private String dataOwnerId = UUID.randomUUID().toString();
    private TwoFactorKey twoFactoryKey;
    private DataOwner dataOwner;

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

        twoFactoryKey = twoFactorKeyGenerator.generate(gpp, userId);
        dataOwner = new DataOwner(dataOwnerId, twoFactoryKey.getPrivateKey());
    }

    @Test
    public void encrypt_decrypt_passes_without_2FA() {
        // encrypt
        CipherText cipherText = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, null);
        assertThat(cipherText).hasNoNullFieldsOrPropertiesExcept("ownerId");
        assertThat(cipherText.isTwoFactorSecured()).isFalse();

        // decrypt
        LinkedHashSet<UserAttributeSecretComponents> userAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponents(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier));
        byte[] output = pairingCryptEngine.decrypt(cipherText, gpp, userId, userAttributeSecretComponents, null);

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
        String attrValueId = "aa.tu-berlin.de.role:student";
        AttributeValueKey attributeKeys2 = attributeValueKeyGenerator.generate(gpp, attrValueId);
        UserAttributeValueKey userAttributeSecretValueKey2 = attributeValueKeyGenerator
                .generateUserKey(gpp, userId, authorityKeys.getPrivateKey(), attributeKeys2.getPrivateKey());

        andAccessPolicy.getAccessPolicyElements().add(
                new AccessPolicyElement(authorityKeys.getPublicKey(), attributeKeys2.getPublicKey(), attrValueId)
        );

        // encrypt
        CipherText cipherText = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, null);
        assertThat(cipherText).hasNoNullFieldsOrPropertiesExcept("ownerId");
        assertThat(cipherText.isTwoFactorSecured()).isFalse();

        // decrypt
        LinkedHashSet<UserAttributeSecretComponents> userAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponents(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier),
                new UserAttributeSecretComponents(userAttributeSecretValueKey2, attributeKeys.getPublicKey(), attrValueId));
        byte[] output = pairingCryptEngine.decrypt(cipherText, gpp, userId, userAttributeSecretComponents, null);

        assertSameElements(output, message);
    }

    @Test
    public void encrypt_decrypt_fails_without_2FA_with_multipleAttributes_sameAuthority_butPolicyNotSatisfied() {
        String attrValueId = "aa.tu-berlin.de.role:student";
        AttributeValueKey attributeKeys2 = attributeValueKeyGenerator.generate(gpp, attrValueId);

        andAccessPolicy.getAccessPolicyElements().add(
                new AccessPolicyElement(authorityKeys.getPublicKey(), attributeKeys2.getPublicKey(), attrValueId)
        );

        // encrypt
        CipherText cipherText = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, null);
        assertThat(cipherText).hasNoNullFieldsOrPropertiesExcept("ownerId");
        assertThat(cipherText.isTwoFactorSecured()).isFalse();

        // decrypt
        LinkedHashSet<UserAttributeSecretComponents> userAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponents(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier));
        assertThatExceptionOfType(AccessPolicyNotSatisfiedException.class).isThrownBy(
                () -> pairingCryptEngine.decrypt(cipherText, gpp, userId, userAttributeSecretComponents, null)
        );
    }

    @Test
    public void encrypt_decrypt_fails_without_2FA_with_multipleAttributes_otherAuthorities() {
        String attrValueId = "aa.hpi.de.role:student";
        AttributeValueKey attributeKeys2 = attributeValueKeyGenerator.generate(gpp, attrValueId);
        AuthorityKey authorityAuthorityKey2 = authorityKeyGenerator.generate(gpp);

        UserAttributeValueKey userAttributeSecretValueKey2 = attributeValueKeyGenerator
                .generateUserKey(gpp, userId, authorityAuthorityKey2.getPrivateKey(), attributeKeys2.getPrivateKey());

        andAccessPolicy.getAccessPolicyElements().add(
                new AccessPolicyElement(authorityAuthorityKey2.getPublicKey(), attributeKeys2.getPublicKey(), attrValueId)
        );

        // encrypt
        CipherText cipherText = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, null);
        assertThat(cipherText).hasNoNullFieldsOrPropertiesExcept("ownerId");
        assertThat(cipherText.isTwoFactorSecured()).isFalse();

        // decrypt
        LinkedHashSet<UserAttributeSecretComponents> userAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponents(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier),
                new UserAttributeSecretComponents(userAttributeSecretValueKey2, attributeKeys.getPublicKey(), attrValueId));
        byte[] output = pairingCryptEngine.decrypt(cipherText, gpp, userId, userAttributeSecretComponents, null);

        assertSameElements(output, message);
    }

    @Test
    public void encrypt_decrypt_fails_without_2FA_with_multipleAttributes_otherAuthorities_butPolicyNotSatisfied() {
        String attrValueId = "aa.hpi.de.role:student";
        AttributeValueKey attributeKeys2 = attributeValueKeyGenerator.generate(gpp, attrValueId);
        AuthorityKey authorityAuthorityKey2 = authorityKeyGenerator.generate(gpp);

        andAccessPolicy.getAccessPolicyElements().add(
                new AccessPolicyElement(authorityAuthorityKey2.getPublicKey(), attributeKeys2.getPublicKey(), attrValueId)
        );

        // encrypt
        CipherText cipherText = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, null);
        assertThat(cipherText).hasNoNullFieldsOrPropertiesExcept("ownerId");
        assertThat(cipherText.isTwoFactorSecured()).isFalse();

        // decrypt
        LinkedHashSet<UserAttributeSecretComponents> userAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponents(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier));
        assertThatExceptionOfType(AccessPolicyNotSatisfiedException.class).isThrownBy(
                () -> pairingCryptEngine.decrypt(cipherText, gpp, userId, userAttributeSecretComponents, null)
        );
    }

    @Test
    public void encrypt_decrypt_passes_without_2FA_withUseProvidingMoreAttributeKeysThenNessecary() {
        String attrValueId = "aa.hpi.de.role:student";
        AttributeValueKey attributeKeys2 = attributeValueKeyGenerator.generate(gpp, attrValueId);
        AuthorityKey authorityAuthorityKey2 = authorityKeyGenerator.generate(gpp);
        UserAttributeValueKey userSecretAttributeValueKey2 = attributeValueKeyGenerator
                .generateUserKey(gpp, userId, authorityAuthorityKey2.getPrivateKey(), attributeKeys2.getPrivateKey());

        // encrypt
        CipherText cipherText = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, null);
        assertThat(cipherText).hasNoNullFieldsOrPropertiesExcept("ownerId");
        assertThat(cipherText.isTwoFactorSecured()).isFalse();

        // decrypt
        LinkedHashSet<UserAttributeSecretComponents> userAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponents(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier),
                new UserAttributeSecretComponents(userSecretAttributeValueKey2, attributeKeys2.getPublicKey(), attrValueId));
        byte[] output = pairingCryptEngine.decrypt(cipherText, gpp, userId, userAttributeSecretComponents, null);

        assertSameElements(output, message);
    }

    @Test
    public void encrypt_decrypt_passes_wit_FA() {
        // encrypt
        CipherText cipherText = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, dataOwner);
        assertThat(cipherText).hasNoNullFieldsOrProperties();
        assertThat(cipherText.isTwoFactorSecured()).isTrue();

        // decrypt
        LinkedHashSet<UserAttributeSecretComponents> userAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponents(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier));
        byte[] output = pairingCryptEngine.decrypt(cipherText, gpp, userId, userAttributeSecretComponents, twoFactoryKey.getPublicKey());

        assertSameElements(output, message);
    }

    @Test
    public void encrypt_decrypt_passes_with_FA_butUserHasNo2FAKey() {
        // encrypt
        CipherText cipherText = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, dataOwner);
        assertThat(cipherText).hasNoNullFieldsOrProperties();
        assertThat(cipherText.isTwoFactorSecured()).isTrue();

        // decrypt
        LinkedHashSet<UserAttributeSecretComponents> userAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponents(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier));
        byte[] output = pairingCryptEngine.decrypt(cipherText, gpp, userId, userAttributeSecretComponents, null);

        assertNotSameElements(output, message);
    }

    @Test
    public void collusionResistance() {
        String attrValueId = "aa.hpi.de.role:student";
        AttributeValueKey attributeKeys2 = attributeValueKeyGenerator.generate(gpp, attrValueId);
        AuthorityKey authorityAuthorityKey2 = authorityKeyGenerator.generate(gpp);
        UserAttributeValueKey userSecretAttributeValueKey2 = attributeValueKeyGenerator
                .generateUserKey(gpp, "OtherId", authorityAuthorityKey2.getPrivateKey(), attributeKeys2.getPrivateKey());

        // encrypt
        andAccessPolicy.getAccessPolicyElements().add(
                new AccessPolicyElement(authorityAuthorityKey2.getPublicKey(), attributeKeys2.getPublicKey(), attrValueId)
        );

        CipherText cipherText = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, null);
        assertThat(cipherText).hasNoNullFieldsOrPropertiesExcept("ownerId");
        assertThat(cipherText.isTwoFactorSecured()).isFalse();

        // decrypt
        LinkedHashSet<UserAttributeSecretComponents> userAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponents(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier),
                new UserAttributeSecretComponents(userSecretAttributeValueKey2, attributeKeys2.getPublicKey(), attrValueId));
        byte[] output = pairingCryptEngine.decrypt(cipherText, gpp, userId, userAttributeSecretComponents, null);

        assertNotSameElements(output, message);
    }
}