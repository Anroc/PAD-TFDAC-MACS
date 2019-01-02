package de.tuberlin.tfdacmacs.centralserver.gpp;

import de.tuberlin.tfdacmacs.crypto.pairing.PairingGenerator;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.centralserver.gpp.db.GlobalPublicParameterDB;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Data
@Service
@Slf4j
@RequiredArgsConstructor
public class GlobalPublicParameterService {

    private final PairingGenerator pairingGenerator;
    private final GlobalPublicParameterDB globalPublicParameterDB;

    public GlobalPublicParameter getGlobalPublicParameter() {
        if(globalPublicParameterDB.gppExist()) {
            return globalPublicParameterDB.findEntity().get();
        } else {
            GlobalPublicParameter globalPublicParameter = createGlobalPublicParameter();
            globalPublicParameterDB.insert(globalPublicParameter);
            return globalPublicParameter;
        }
    }

    private GlobalPublicParameter createGlobalPublicParameter() {
        PairingParameters pairingParameters = pairingGenerator.generateNewTypeACurveParameter();
        Pairing pairing = pairingGenerator.setupPairing(pairingParameters);
        Element g = pairing.getG1().newRandomElement().getImmutable();
        return new GlobalPublicParameter(pairing, pairingParameters, g);
    }


}
