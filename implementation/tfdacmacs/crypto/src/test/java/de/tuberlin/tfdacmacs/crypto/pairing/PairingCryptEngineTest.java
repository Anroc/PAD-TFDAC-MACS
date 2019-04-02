package de.tuberlin.tfdacmacs.crypto.pairing;

import de.tuberlin.tfdacmacs.crypto.UnitTestSuite;
import de.tuberlin.tfdacmacs.crypto.pairing.data.*;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AuthorityKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.TwoFactorKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.UserAttributeValueKey;
import de.tuberlin.tfdacmacs.crypto.pairing.exceptions.AccessPolicyNotSatisfiedException;
import de.tuberlin.tfdacmacs.crypto.pairing.exceptions.TwoFactorContrainNotStatisfiedException;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

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
        attributeKeys = attributeValueKeyGenerator.generateNew(gpp, attributeValueIdentifier);
        userSecretAttributeValueKey = attributeValueKeyGenerator
                .generateUserKey(gpp, userId, authorityKeys.getPrivateKey(), attributeKeys.getPrivateKey());

        HashSet<AttributePolicyElement> attributePolicyElements = Sets.newHashSet();
        attributePolicyElements.add(new AttributePolicyElement(authorityKeys.getPublicKey(), attributeKeys.getPublicKey(), new VersionedID(attributeValueIdentifier, attributeKeys.getVersion())));
        andAccessPolicy = new AndAccessPolicy(attributePolicyElements);

        twoFactoryKey = twoFactorKeyGenerator.generateNew(gpp, userId);
        dataOwner = new DataOwner(dataOwnerId, twoFactoryKey.getPrivateKey());
    }

    @Test
    public void encrypt_decrypt_passes_without_2FA() {
        // encrypt
        AndCipherText andCipherText = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, null);
        CipherText cipherText = andCipherText.getCipherText();

        UserAttributeSecretComponent userAttributeSecretComponent = new UserAttributeSecretComponent(
                userSecretAttributeValueKey, attributeKeys.getPublicKey(),
                attributeValueIdentifier);
        assertSuccessfulDecryption(cipherText, andCipherText.getFile(), userAttributeSecretComponent);
    }

    @Test
    public void encrypt_decrypt_passes_without_2FA_sameEncryptionDoesNotResolveInSameCipherText() {
        // encrypt
        AndCipherText andCipherText1 = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, null);
        CipherText cipherText1 = andCipherText1.getCipherText();
        AndCipherText andCipherText2 = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, null);
        CipherText cipherText2 = andCipherText2.getCipherText();
        assertNotSameElements(andCipherText1.getFile().getData(), andCipherText2.getFile().getData());
        assertThat(cipherText1.getC1()).isNotEqualTo(cipherText2.getC1());
        assertThat(cipherText1.getC2()).isNotEqualTo(cipherText2.getC2());
        assertThat(cipherText1.getC3()).isNotEqualTo(cipherText2.getC3());
    }

    @Test
    public void encrypt_decrypt_passes_without_2FA_with_multipleAttributes_sameAuthority() {
        String attrValueId = "aa.tu-berlin.de.role:student";
        AttributeValueKey attributeKeys2 = attributeValueKeyGenerator.generateNew(gpp, attrValueId);
        UserAttributeValueKey userAttributeSecretValueKey2 = attributeValueKeyGenerator
                .generateUserKey(gpp, userId, authorityKeys.getPrivateKey(), attributeKeys2.getPrivateKey());

        andAccessPolicy.getAttributePolicyElements().add(
                new AttributePolicyElement(authorityKeys.getPublicKey(), attributeKeys2.getPublicKey(), new VersionedID(attrValueId, attributeKeys2.getVersion()))
        );

        // encrypt
        AndCipherText andCipherText = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, null);
        CipherText cipherText = andCipherText.getCipherText();
        assertThat(cipherText).hasNoNullFieldsOrPropertiesExcept("ownerId");
        assertThat(cipherText.isTwoFactorSecured()).isFalse();

        // decrypt
        assertSuccessfulDecryption(
                cipherText,
                andCipherText.getFile(),
                new UserAttributeSecretComponent(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier),
                new UserAttributeSecretComponent(userAttributeSecretValueKey2, attributeKeys.getPublicKey(), new VersionedID(attrValueId, attributeKeys.getVersion()))
        );
    }

    @Test
    public void encrypt_decrypt_fails_without_2FA_with_multipleAttributes_sameAuthority_butPolicyNotSatisfied() {
        String attrValueId = "aa.tu-berlin.de.role:student";
        AttributeValueKey attributeKeys2 = attributeValueKeyGenerator.generateNew(gpp, attrValueId);

        andAccessPolicy.getAttributePolicyElements().add(
                new AttributePolicyElement(authorityKeys.getPublicKey(), attributeKeys2.getPublicKey(), new VersionedID(attrValueId, attributeKeys2.getVersion()))
        );

        // encrypt
        AndCipherText andCipherText = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, null);
        CipherText cipherText = andCipherText.getCipherText();
        assertThat(cipherText).hasNoNullFieldsOrPropertiesExcept("ownerId");
        assertThat(cipherText.isTwoFactorSecured()).isFalse();

        // decrypt
        LinkedHashSet<UserAttributeSecretComponent> userAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponent(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier));
        assertThatExceptionOfType(AccessPolicyNotSatisfiedException.class).isThrownBy(
                () -> pairingCryptEngine.decrypt(andCipherText.getFile().getData(), cipherText, gpp, userId, userAttributeSecretComponents, null)
        );
    }

    @Test
    public void encrypt_decrypt_passes_without_2FA_with_multipleAttributes_otherAuthorities() {
        String attrValueId = "aa.hpi.de.role:student";
        AttributeValueKey attributeKeys2 = attributeValueKeyGenerator.generateNew(gpp, attrValueId);
        AuthorityKey authorityAuthorityKey2 = authorityKeyGenerator.generate(gpp);

        UserAttributeValueKey userAttributeSecretValueKey2 = attributeValueKeyGenerator
                .generateUserKey(gpp, userId, authorityAuthorityKey2.getPrivateKey(), attributeKeys2.getPrivateKey());

        andAccessPolicy.getAttributePolicyElements().add(
                new AttributePolicyElement(authorityAuthorityKey2.getPublicKey(), attributeKeys2.getPublicKey(), new VersionedID(attrValueId, attributeKeys2.getVersion()))
        );

        // encrypt
        AndCipherText andCipherText = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, null);
        CipherText cipherText = andCipherText.getCipherText();
        assertThat(cipherText).hasNoNullFieldsOrPropertiesExcept("ownerId");
        assertThat(cipherText.isTwoFactorSecured()).isFalse();

        // decrypt
        assertSuccessfulDecryption(
                cipherText,
                andCipherText.getFile(),
                new UserAttributeSecretComponent(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier),
                new UserAttributeSecretComponent(userAttributeSecretValueKey2, attributeKeys.getPublicKey(), new VersionedID(attrValueId, attributeKeys.getVersion()))
        );
    }

    @Test
    public void encrypt_decrypt_fails_without_2FA_with_multipleAttributes_otherAuthorities_butPolicyNotSatisfied() {
        String attrValueId = "aa.hpi.de.role:student";
        AttributeValueKey attributeKeys2 = attributeValueKeyGenerator.generateNew(gpp, attrValueId);
        AuthorityKey authorityAuthorityKey2 = authorityKeyGenerator.generate(gpp);

        andAccessPolicy.getAttributePolicyElements().add(
                new AttributePolicyElement(authorityAuthorityKey2.getPublicKey(), attributeKeys2.getPublicKey(), new VersionedID(attrValueId, attributeKeys2.getVersion()))
        );

        // encrypt
        AndCipherText andCipherText = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, null);
        CipherText cipherText = andCipherText.getCipherText();
        assertThat(cipherText).hasNoNullFieldsOrPropertiesExcept("ownerId");
        assertThat(cipherText.isTwoFactorSecured()).isFalse();

        // decrypt
        LinkedHashSet<UserAttributeSecretComponent> userAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponent(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier));
        assertThatExceptionOfType(AccessPolicyNotSatisfiedException.class).isThrownBy(
                () -> pairingCryptEngine.decrypt(andCipherText.getFile().getData(), cipherText, gpp, userId, userAttributeSecretComponents, null)
        );
    }

    @Test
    public void encrypt_decrypt_passes_without_2FA_withUseProvidingMoreAttributeKeysThenNessecary() {
        String attrValueId = "aa.hpi.de.role:student";
        AttributeValueKey attributeKeys2 = attributeValueKeyGenerator.generateNew(gpp, attrValueId);
        AuthorityKey authorityAuthorityKey2 = authorityKeyGenerator.generate(gpp);
        UserAttributeValueKey userSecretAttributeValueKey2 = attributeValueKeyGenerator
                .generateUserKey(gpp, userId, authorityAuthorityKey2.getPrivateKey(), attributeKeys2.getPrivateKey());

        // encrypt
        AndCipherText andCipherText = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, null);
        CipherText cipherText = andCipherText.getCipherText();
        assertThat(cipherText).hasNoNullFieldsOrPropertiesExcept("ownerId");
        assertThat(cipherText.isTwoFactorSecured()).isFalse();

        // decrypt
        assertSuccessfulDecryption(
                cipherText,
                andCipherText.getFile(),
                new UserAttributeSecretComponent(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier),
                new UserAttributeSecretComponent(userSecretAttributeValueKey2, attributeKeys.getPublicKey(), new VersionedID(attrValueId, attributeKeys.getVersion()))
        );
    }

    @Test
    public void encrypt_decrypt_passes_wit_FA() {
        // encrypt
        AndCipherText andCipherText = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, dataOwner);
        CipherText cipherText = andCipherText.getCipherText();
        assertThat(cipherText).hasNoNullFieldsOrProperties();
        assertThat(cipherText.isTwoFactorSecured()).isTrue();

        // decrypt
        LinkedHashSet<UserAttributeSecretComponent> userAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponent(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier));
        byte[] output = pairingCryptEngine.decrypt(andCipherText.getFile().getData(), cipherText, gpp, userId, userAttributeSecretComponents, twoFactoryKey.getSecretKeyOfUser(userId));

        assertSameElements(output, message);
    }

    @Test
    public void encrypt_decrypt_passes_with_FA_butUserHasNo2FAKey() {
        // encrypt
        AndCipherText andCipherText = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, dataOwner);
        CipherText cipherText = andCipherText.getCipherText();
        assertThat(cipherText).hasNoNullFieldsOrProperties();
        assertThat(cipherText.isTwoFactorSecured()).isTrue();

        // decrypt
        LinkedHashSet<UserAttributeSecretComponent> userAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponent(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier));
        assertThatExceptionOfType(TwoFactorContrainNotStatisfiedException.class).isThrownBy(
                () -> pairingCryptEngine.decrypt(andCipherText.getFile().getData(), cipherText, gpp, userId, userAttributeSecretComponents, null)
        );
    }

    @Test
    public void collusionResistance() {
        String attrValueId = "aa.hpi.de.role:student";
        AttributeValueKey attributeKeys2 = attributeValueKeyGenerator.generateNew(gpp, attrValueId);
        AuthorityKey authorityAuthorityKey2 = authorityKeyGenerator.generate(gpp);
        UserAttributeValueKey userSecretAttributeValueKey2 = attributeValueKeyGenerator
                .generateUserKey(gpp, "OtherId", authorityAuthorityKey2.getPrivateKey(), attributeKeys2.getPrivateKey());

        // encrypt
        andAccessPolicy.getAttributePolicyElements().add(
                new AttributePolicyElement(authorityAuthorityKey2.getPublicKey(), attributeKeys2.getPublicKey(), new VersionedID(attrValueId, attributeKeys2.getVersion()))
        );

        AndCipherText andCipherText = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, null);
        CipherText cipherText = andCipherText.getCipherText();
        assertThat(cipherText).hasNoNullFieldsOrPropertiesExcept("ownerId");
        assertThat(cipherText.isTwoFactorSecured()).isFalse();

        // decrypt
        LinkedHashSet<UserAttributeSecretComponent> userAttributeSecretComponents = Sets.newLinkedHashSet(
                new UserAttributeSecretComponent(userSecretAttributeValueKey, attributeKeys.getPublicKey(), attributeValueIdentifier),
                new UserAttributeSecretComponent(userSecretAttributeValueKey2, attributeKeys2.getPublicKey(), new VersionedID(attrValueId, attributeKeys2.getVersion())));
        byte[] output = pairingCryptEngine.decrypt(andCipherText.getFile().getData(), cipherText, gpp, userId, userAttributeSecretComponents, null);

        assertNotSameElements(output, message);
    }

    @Test
    public void encrypt_decrypt_passes_withDNFAccessPolicy() {
        String attrValueId = "aa.tu-berlin.de.role:student";
        AttributeValueKey attributeKeys2 = attributeValueKeyGenerator.generateNew(gpp, attrValueId);
        UserAttributeValueKey userAttributeSecretValueKey2 = attributeValueKeyGenerator
                .generateUserKey(gpp, userId, authorityKeys.getPrivateKey(), attributeKeys2.getPrivateKey());

        AndAccessPolicy andAccessPolicy2 = new AndAccessPolicy(
                Sets.newLinkedHashSet(new AttributePolicyElement(authorityKeys.getPublicKey(), attributeKeys2.getPublicKey(), new VersionedID(attrValueId, attributeKeys2.getVersion())))
        );
        DNFAccessPolicy dnfAccessPolicy = new DNFAccessPolicy(
                Lists.newArrayList(andAccessPolicy, andAccessPolicy2)
        );


        // encrypt
        DNFCipherText dnfCipherText = pairingCryptEngine.encrypt(message, dnfAccessPolicy, gpp, null);
        List<CipherText> cipherTexts = dnfCipherText.getCipherTexts();
        assertThat(cipherTexts).hasSize(2);

        // assert frist ciphertext
        CipherText cipherText1 = cipherTexts.get(0);
        UserAttributeSecretComponent userAttributeSecretComponent = new UserAttributeSecretComponent(
                userSecretAttributeValueKey, attributeKeys.getPublicKey(),
                attributeValueIdentifier);
        assertSuccessfulDecryption(cipherText1, dnfCipherText.getFile(), userAttributeSecretComponent);

        CipherText cipherText2 = cipherTexts.get(1);
        UserAttributeSecretComponent userAttributeSecretComponent2 = new UserAttributeSecretComponent(
                userAttributeSecretValueKey2, attributeKeys2.getPublicKey(),
                attrValueId);
        assertSuccessfulDecryption(cipherText2, dnfCipherText.getFile(), userAttributeSecretComponent2);
    }

    private void assertSuccessfulDecryption(CipherText cipherText1, File file, UserAttributeSecretComponent component, UserAttributeSecretComponent... components) {
        assertThat(cipherText1).hasNoNullFieldsOrPropertiesExcept("ownerId");
        assertThat(cipherText1.isTwoFactorSecured()).isFalse();

        // decrypt
        Set<UserAttributeSecretComponent> userAttributeSecretComponents = Sets.newLinkedHashSet(components);
        userAttributeSecretComponents.add(component);
        byte[] output = pairingCryptEngine.decrypt(
                file.getData(),
                cipherText1,
                gpp,
                userId,
                userAttributeSecretComponents,
                null
        );

        assertSameElements(output, message);
    }
}