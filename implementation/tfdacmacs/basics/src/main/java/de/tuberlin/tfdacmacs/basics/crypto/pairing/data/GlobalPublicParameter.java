package de.tuberlin.tfdacmacs.basics.crypto.pairing.data;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.security.PublicKey;

@Data
@RequiredArgsConstructor
public class GlobalPublicParameter {

    private final Pairing pairing;
    private final PairingParameters pairingParameters;
    private final Element g;

    private final PublicKey rsaPublicKey;

    public Element getG() {
        return g.duplicate();
    }
}
