package de.tuberlin.tfdacmacs.centralserver.gpp.data;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class GlobalPublicParameter {

    private final Pairing pairing;
    private final PairingParameters pairingParameters;
    private final Element g;
}
