package de.tuberlin.tfdacmacs.basics.crypto.pairing;

import de.tuberlin.tfdacmacs.basics.UnitTestSuite;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.*;
import it.unisa.dia.gas.jpbc.Element;
import org.assertj.core.util.Sets;
import org.junit.Test;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class PairingCryptEngineTest extends UnitTestSuite {

    private final byte[] message = "Hello World!".getBytes();
    private final String userId = UUID.randomUUID().toString();

    @Test
    public void encrypt_decrypt_passes_without_2FA() {


        GlobalPublicParameter gpp = gppTestFactory.create();
        Key authorityKeys = authorityKeyGenerator.generateAuthorityKey(gpp);
        Key attributeKeys = attributeKeyGenerator.generateAttributeValueKey(gpp);
        Element userSecretAttributeValueKey = attributeKeyGenerator
                .generateSecretUserKey(gpp, userId, authorityKeys.getPrivateKey(), attributeKeys.getPrivateKey());

        HashSet<AccessPolicyElement> accessPolicyElements = Sets.newHashSet();
        accessPolicyElements.add(new AccessPolicyElement(authorityKeys.getPublicKey(), attributeKeys.getPublicKey()));
        AndAccessPolicy andAccessPolicy = new AndAccessPolicy(accessPolicyElements);

        // encrypt
        CipherText cipherText = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, null);
        assertThat(cipherText).hasNoNullFieldsOrPropertiesExcept("ownerId");

        // decrypt
        LinkedHashSet<AttributeSecretComponents> attributeSecretComponents = Sets.newLinkedHashSet(
                new AttributeSecretComponents(userSecretAttributeValueKey, attributeKeys.getPublicKey()));
        byte[] output = pairingCryptEngine.decrypt(cipherText, gpp, userId, attributeSecretComponents, null);

        assertSameElements(output, message);
    }

}