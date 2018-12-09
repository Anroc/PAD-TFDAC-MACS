package de.tuberlin.tfdacmacs.basics;

import de.tuberlin.tfdacmacs.basics.crypto.PairingGenerator;
import de.tuberlin.tfdacmacs.basics.factory.GPPTestFactory;

public class UnitTestSuite {

    protected PairingGenerator pairingGenerator = new PairingGenerator();
    protected GPPTestFactory gppTestFactory = new GPPTestFactory(pairingGenerator);
}
