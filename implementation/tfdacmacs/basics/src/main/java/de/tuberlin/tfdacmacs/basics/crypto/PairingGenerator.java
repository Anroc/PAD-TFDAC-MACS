package de.tuberlin.tfdacmacs.basics.crypto;

import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.jpbc.PairingParametersGenerator;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;
import it.unisa.dia.gas.plaf.jpbc.pbc.curve.PBCTypeACurveGenerator;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PairingGenerator {

    private final PairingFactory pairingFactory;

    public PairingGenerator() {
        this.pairingFactory = PairingFactory.getInstance();
        this.pairingFactory.setUsePBCWhenPossible(true);

        if(! this.pairingFactory.isPBCAvailable()) {
            log.warn("jPBC native extension not available");
        }
    }


    public Pairing setupPairing() {
        return setupPairing(generateNewTypeACurveParameter());
    }

    public Pairing setupPairing(@NonNull PairingParameters parameters) {
        return this.pairingFactory.initPairing(parameters);
    }

    /**
     * Generates parameter for a new type A elliptic pairing curve.
     * <a href=http://gas.dia.unisa.it/projects/jpbc/docs/ecpg.html>jPBC Type A</a>
     * or <a href="https://crypto.stanford.edu/pbc/manual/ch08s03.html">PBC</a>
     * for more information.
     *
     * @return new type A pairing parameters
     */
    public PairingParameters generateNewTypeACurveParameter() {
        // TODO: make configurable?
        int rBits = 160;
        int qBits = 512;

        log.info("Creating new GPP");
        PairingParametersGenerator ppg;
        if(nativeExtensionAvailable()) {
            ppg = new PBCTypeACurveGenerator(rBits, qBits);
        } else {

            ppg = new TypeACurveGenerator(rBits, qBits);
        }

        return ppg.generate();
    }

    public boolean nativeExtensionAvailable() {
        return this.pairingFactory.isPBCAvailable();
    }
}
