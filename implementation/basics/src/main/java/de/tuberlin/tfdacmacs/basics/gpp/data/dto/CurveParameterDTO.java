package de.tuberlin.tfdacmacs.basics.gpp.data.dto;

import de.tuberlin.tfdacmacs.basics.gpp.data.PairingType;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.parameters.PropertiesParameters;
import lombok.Data;

import java.math.BigInteger;

@Data
public class CurveParameterDTO {

    private char type = 'a';

    private BigInteger q;
    private BigInteger h;
    private BigInteger r;
    private int exp1;
    private int exp2;
    private int sign1;
    private int sign2;

    public PairingParameters toPairingParameter() {
        PropertiesParameters propertiesParameters = new PropertiesParameters();
        propertiesParameters.put("type", String.valueOf(type));
        propertiesParameters.put("q", String.valueOf(q));
        propertiesParameters.put("h", String.valueOf(h));
        propertiesParameters.put("r", String.valueOf(r));
        propertiesParameters.put("exp1", String.valueOf(exp1));
        propertiesParameters.put("exp2", String.valueOf(exp2));
        propertiesParameters.put("sign1", String.valueoO(sign1));
        propertiesParameters.put("sign2", String.valueoO(sign2));
        return propertiesParameters;
    }
}
