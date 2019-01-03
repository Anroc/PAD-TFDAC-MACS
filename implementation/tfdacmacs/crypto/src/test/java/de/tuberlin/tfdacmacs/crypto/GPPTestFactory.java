package de.tuberlin.tfdacmacs.crypto;

import de.tuberlin.tfdacmacs.crypto.pairing.PairingGenerator;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.crypto.rsa.StringAsymmetricCryptEngine;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class GPPTestFactory {

    private final PairingGenerator pairingGenerator;
    private final StringAsymmetricCryptEngine cryptEngine;

    public GlobalPublicParameter create() {
        PairingParameters pairingParameters = pairingGenerator.generateNewTypeACurveParameter();
        Pairing pairing = pairingGenerator.setupPairing(pairingParameters);
        GlobalPublicParameter globalPublicParameter = new GlobalPublicParameter(
                pairing, pairingParameters, pairing.getG1().newRandomElement().getImmutable());
        return globalPublicParameter;
    }
}
