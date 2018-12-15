package de.tuberlin.tfdacmacs.basics;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.*;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.util.HashGenerator;
import de.tuberlin.tfdacmacs.basics.crypto.rsa.StringAsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.basics.factory.GPPTestFactory;

public class UnitTestSuite {

    protected PairingGenerator pairingGenerator = new PairingGenerator();
    protected StringAsymmetricCryptEngine rsaCryptEngine = new StringAsymmetricCryptEngine();
    protected HashGenerator hashGenerator = new HashGenerator();
    protected GPPTestFactory gppTestFactory = new GPPTestFactory(pairingGenerator, rsaCryptEngine);
    protected AuthorityKeyGenerator authorityKeyGenerator = new AuthorityKeyGenerator();
    protected AttributeKeyGenerator attributeKeyGenerator = new AttributeKeyGenerator(hashGenerator);
    protected TwoFactorKeyGenerator twoFactorKeyGenerator = new TwoFactorKeyGenerator(hashGenerator);

    protected CryptEngine cryptEngine = new CryptEngine();
}
