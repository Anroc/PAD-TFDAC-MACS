package de.tuberlin.tfdacmacs.basics;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.*;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.util.HashGenerator;
import de.tuberlin.tfdacmacs.basics.crypto.rsa.StringAsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.basics.crypto.rsa.StringSymmetricCryptEngine;
import de.tuberlin.tfdacmacs.basics.factory.GPPTestFactory;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class UnitTestSuite {

    protected PairingGenerator pairingGenerator = new PairingGenerator();
    protected StringAsymmetricCryptEngine rsaCryptEngine = new StringAsymmetricCryptEngine();
    protected StringSymmetricCryptEngine aesCryptEngine = new StringSymmetricCryptEngine();
    protected HashGenerator hashGenerator = new HashGenerator();
    protected GPPTestFactory gppTestFactory = new GPPTestFactory(pairingGenerator, rsaCryptEngine);
    protected AuthorityKeyGenerator authorityKeyGenerator = new AuthorityKeyGenerator();
    protected AttributeKeyGenerator attributeKeyGenerator = new AttributeKeyGenerator(hashGenerator);
    protected TwoFactorKeyGenerator twoFactorKeyGenerator = new TwoFactorKeyGenerator(hashGenerator);

    protected PairingCryptEngine pairingCryptEngine = new PairingCryptEngine(aesCryptEngine, hashGenerator);

    public void assertSameElements(byte[] actual, byte[] expected) {
        assertThat(actual.length).isEqualTo(expected.length);
        for(int i = 0; i<actual.length; i ++) {
            assertThat(actual[i]).isSameAs(expected[i]);
        }
    }
}
