package de.tuberlin.tfdacmacs.basics;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.*;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.aes.AESDecryptor;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.aes.AESEncryptor;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.util.HashGenerator;
import de.tuberlin.tfdacmacs.basics.crypto.rsa.StringAsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.basics.crypto.rsa.StringSymmetricCryptEngine;
import de.tuberlin.tfdacmacs.basics.factory.GPPTestFactory;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.fail;

public class UnitTestSuite {

    protected PairingGenerator pairingGenerator = new PairingGenerator();
    protected StringAsymmetricCryptEngine rsaCryptEngine = new StringAsymmetricCryptEngine();
    protected StringSymmetricCryptEngine aesCryptEngine = new StringSymmetricCryptEngine();
    protected HashGenerator hashGenerator = new HashGenerator();
    protected GPPTestFactory gppTestFactory = new GPPTestFactory(pairingGenerator, rsaCryptEngine);
    protected AuthorityKeyGenerator authorityKeyGenerator = new AuthorityKeyGenerator();
    protected AttributeValueKeyGenerator attributeValueKeyGenerator = new AttributeValueKeyGenerator(hashGenerator);
    protected TwoFactorKeyGenerator twoFactorKeyGenerator = new TwoFactorKeyGenerator(hashGenerator);

    protected AESEncryptor aesEncryptor = new AESEncryptor(hashGenerator, aesCryptEngine);
    protected AESDecryptor aesDecryptor = new AESDecryptor(hashGenerator, aesCryptEngine);

    protected ABEEncryptor abeEncryptor = new ABEEncryptor();
    protected ABEDecryptor abeDecryptor = new ABEDecryptor(hashGenerator);

    protected PairingCryptEngine pairingCryptEngine =
            new PairingCryptEngine(aesEncryptor, aesDecryptor, abeEncryptor, abeDecryptor);

    public void assertSameElements(byte[] actual, byte[] expected) {
        assertThat(actual.length).isEqualTo(expected.length);
        for(int i = 0; i<actual.length; i ++) {
            assertThat(actual[i]).isSameAs(expected[i]);
        }
    }

    public void assertNotSameElements(byte[] actual, byte[] expected) {
        if(actual.length != expected.length) {
            return;
        }
        for(int i = 0; i<actual.length; i ++) {
            if(actual[i] != expected[i]) {
                return;
            }
        }
        fail("Arrays are identical.");
    }
}
