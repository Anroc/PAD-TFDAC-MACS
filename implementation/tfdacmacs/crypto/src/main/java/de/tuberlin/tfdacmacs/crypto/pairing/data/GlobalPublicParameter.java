package de.tuberlin.tfdacmacs.crypto.pairing.data;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import lombok.Data;

@Data
public class GlobalPublicParameter {

    private final Pairing pairing;
    private final PairingParameters pairingParameters;
    private final Element g;

    public GlobalPublicParameter(Pairing pairing, PairingParameters pairingParameters, Element g) {
        this.pairing = pairing;
        this.pairingParameters = pairingParameters;
        this.g = g.getImmutable();
    }

    public Element getG() {
        return g.duplicate();
    }

    public Field g1() {
        return getPairing().getG1();
    }

    public Field zr() {
        return getPairing().getZr();
    }

    public Field gt() {
        return getPairing().getGT();
    }
}
