package de.tuberlin.tfdacmacs.client.gpp.client.dto;

import de.tuberlin.tfdacmacs.crypto.pairing.PairingGenerator;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlobalPublicParameterDTO  {

    @NotNull
    private CurveParameterDTO curveParameter;

    @NotNull
    private GeneratorDTO generator;

    public GlobalPublicParameter toGlobalPublicParameter(@NonNull PairingGenerator pairingGenerator) {
        PairingParameters pairingParameters = getCurveParameter().toPairingParameter();
        Pairing pairing = pairingGenerator.setupPairing(pairingParameters);
        Element g = getGenerator().toElement(pairing.getG1());
        return new GlobalPublicParameter(pairing, pairingParameters, g);
    }
}
