package de.tuberlin.tfdacmacs.basics.gpp.data.dto;

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
        propertiesParameters.put("sign1", String.valueOf(sign1));
        propertiesParameters.put("sign2", String.valueOf(sign2));
        return propertiesParameters;
    }

    public static CurveParameterDTO from(PairingParameters pairingParameters) {
        CurveParameterDTO curveParameterDTO = new CurveParameterDTO();
        curveParameterDTO.setQ(pairingParameters.getBigInteger("q"));
        curveParameterDTO.setH(pairingParameters.getBigInteger("h"));
        curveParameterDTO.setR(pairingParameters.getBigInteger("r"));
        curveParameterDTO.setExp1(pairingParameters.getInt("exp1"));
        curveParameterDTO.setExp2(pairingParameters.getInt("exp2"));
        curveParameterDTO.setSign1(pairingParameters.getInt("sign1"));
        curveParameterDTO.setSign2(pairingParameters.getInt("sign2"));
        return curveParameterDTO;
    }
}
