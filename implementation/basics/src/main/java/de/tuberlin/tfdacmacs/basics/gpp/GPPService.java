package de.tuberlin.tfdacmacs.basics.gpp;

import de.tuberlin.tfdacmacs.basics.crypto.PairingGenerator;
import it.unisa.dia.gas.jpbc.Pairing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GPPService {

    private final PairingGenerator pairingGenerator;

    @Autowired
    public GPPService(PairingGenerator pairingGenerator) {
        this.pairingGenerator = pairingGenerator;
    }

    public void generateGlobalPublicParameter() {
        Pairing pairing = pairingGenerator.setupPairing();
    }
}
