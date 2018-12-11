package de.tuberlin.tfdacmacs.basics;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.PairingGenerator;
import de.tuberlin.tfdacmacs.basics.crypto.rsa.StringAsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.basics.factory.GPPTestFactory;

public class UnitTestSuite {

    protected PairingGenerator pairingGenerator = new PairingGenerator();
    protected StringAsymmetricCryptEngine cryptEngine = new StringAsymmetricCryptEngine();
    protected GPPTestFactory gppTestFactory = new GPPTestFactory(pairingGenerator, cryptEngine);
}
