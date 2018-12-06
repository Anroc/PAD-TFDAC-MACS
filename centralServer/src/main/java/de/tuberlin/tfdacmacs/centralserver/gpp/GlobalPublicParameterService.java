package de.tuberlin.tfdacmacs.centralserver.gpp;

import de.tuberlin.tfdacmacs.basics.crypto.PairingGenerator;
import de.tuberlin.tfdacmacs.centralserver.gpp.data.GlobalPublicParameter;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Data
@Service
public class GlobalPublicParameterService {

    private GlobalPublicParameter globalPublicParameter;

    @Autowired
    private PairingGenerator pairingGenerator;

    public GlobalPublicParameter createOrGetGPP() {
        if (globalPublicParameter == null) {
            return createGlobalPublicParameter();
        } else {
            return this.getGlobalPublicParameter();
        }
    }

    private GlobalPublicParameter createGlobalPublicParameter() {
        PairingParameters pairingParameters = pairingGenerator.generateNewTypeACurveParameter();
        Pairing pairing = pairingGenerator.setupPairing(pairingParameters);
        Element g = pairing.getG1().newRandomElement();
        return new GlobalPublicParameter(pairing, pairingParameters, g);
    }
}
