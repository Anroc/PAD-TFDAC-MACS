package de.tuberlin.tfdacmacs.centralserver.gpp;

import de.tuberlin.tfdacmacs.basics.crypto.PairingGenerator;
import de.tuberlin.tfdacmacs.basics.gpp.data.GlobalPublicParameter;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Data
@Service
@Slf4j
public class GlobalPublicParameterService {

    private GlobalPublicParameter globalPublicParameter;

    @Autowired
    private PairingGenerator pairingGenerator;

    public GlobalPublicParameter createOrGetGPP() {
        if (globalPublicParameter == null) {
            this.globalPublicParameter = createGlobalPublicParameter();
        }
        return this.getGlobalPublicParameter();
    }

    private GlobalPublicParameter createGlobalPublicParameter() {
        PairingParameters pairingParameters = pairingGenerator.generateNewTypeACurveParameter();
        Pairing pairing = pairingGenerator.setupPairing(pairingParameters);
        Element g = pairing.getG1().newRandomElement();
        return new GlobalPublicParameter(pairing, pairingParameters, g);
    }
}
