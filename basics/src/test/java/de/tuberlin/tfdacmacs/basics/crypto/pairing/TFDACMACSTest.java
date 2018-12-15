package de.tuberlin.tfdacmacs.basics.crypto.pairing;

import de.tuberlin.tfdacmacs.basics.UnitTestSuite;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.*;
import org.assertj.core.util.Sets;
import org.junit.Test;

import java.util.HashSet;

public class TFDACMACSTest extends UnitTestSuite {

    private final byte[] message = "Hello World!".getBytes();

    @Test
    public void tfdacmacs_encrypt_decrypt_passes_without_2FA() {
        GlobalPublicParameter gpp = gppTestFactory.create();
        Key authorityKeys = authorityKeyGenerator.generateAuthorityKey(gpp);
        Key attributeKeys = attributeKeyGenerator.generateAttributeValueKey(gpp);

        HashSet<AccessPolicyElement> accessPolicyElements = Sets.newHashSet();
        accessPolicyElements.add(new AccessPolicyElement(authorityKeys.getPublicKey(), attributeKeys.getPublicKey()));
        AndAccessPolicy andAccessPolicy = new AndAccessPolicy(accessPolicyElements);

        CipherText encrypt = cryptEngine.encrypt(message, andAccessPolicy, gpp, null);
    }

}