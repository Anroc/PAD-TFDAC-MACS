package de.tuberlin.tfdacmacs.basics.gpp.data.dto;

import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.parameters.PropertiesParameters;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;

@Data
@NoArgsConstructor
public class CurveParameterDTO {

    @NotBlank
    private char type = 'a';

    @NotBlank
    private String q;
    @NotBlank
    private String h;
    @NotBlank
    private String r;

    private int exp1;
    private int exp2;
    private int sign0;
    private int sign1;

    public PairingParameters toPairingParameter() {
        PropertiesParameters propertiesParameters = new PropertiesParameters();
        propertiesParameters.put("type", String.valueOf(type));
        propertiesParameters.put("q", q);
        propertiesParameters.put("h", h);
        propertiesParameters.put("r", r);
        propertiesParameters.put("exp1", String.valueOf(exp1));
        propertiesParameters.put("exp2", String.valueOf(exp2));
        propertiesParameters.put("sign0", String.valueOf(sign0));
        propertiesParameters.put("sign0", String.valueOf(sign1));
        return propertiesParameters;
    }

    public static CurveParameterDTO from(@NonNull PairingParameters pairingParameters) {
        CurveParameterDTO curveParameterDTO = new CurveParameterDTO();
        curveParameterDTO.setQ(pairingParameters.getString("q"));
        curveParameterDTO.setH(pairingParameters.getString("h"));
        curveParameterDTO.setR(pairingParameters.getString("r"));
        curveParameterDTO.setExp1(pairingParameters.getInt("exp1"));
        curveParameterDTO.setExp2(pairingParameters.getInt("exp2"));
        curveParameterDTO.setSign0(pairingParameters.getInt("sign0"));
        curveParameterDTO.setSign1(pairingParameters.getInt("sign0"));
        return curveParameterDTO;
    }
}
