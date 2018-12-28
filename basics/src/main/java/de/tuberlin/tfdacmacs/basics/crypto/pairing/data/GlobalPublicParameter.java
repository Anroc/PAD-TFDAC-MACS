package de.tuberlin.tfdacmacs.basics.crypto.pairing.data;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import lombok.Data;

import java.security.PublicKey;

@Data
public class GlobalPublicParameter {

    private final Pairing pairing;
    private final PairingParameters pairingParameters;
    private final Element g;

    private final PublicKey rsaPublicKey;

    public GlobalPublicParameter(Pairing pairing, PairingParameters pairingParameters, Element g,
            PublicKey rsaPublicKey) {
        this.pairing = pairing;
        this.pairingParameters = pairingParameters;
        this.g = g.getImmutable();
        this.rsaPublicKey = rsaPublicKey;
    }

    public Element getG() {
        return g.duplicate();
    }
}
