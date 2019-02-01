package de.tuberlin.tfdacmacs.client.gpp.client.dto;

import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.parameters.PropertiesParameters;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

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
        propertiesParameters.put("sign1", String.valueOf(sign1));
        return propertiesParameters;
    }
}
