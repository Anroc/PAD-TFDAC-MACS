package de.tuberlin.tfdacmacs.lib.gpp.data.dto;

import com.couchbase.client.java.repository.annotation.Field;
import de.tuberlin.tfdacmacs.crypto.pairing.PairingGenerator;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.lib.db.Entity;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class GlobalPublicParameterDTO extends Entity {

    public static final String ID = "GLOBAL_PUBLIC_PARAMETER";

    public GlobalPublicParameterDTO() {
        super(ID);
    }

    public GlobalPublicParameterDTO(
            @NotNull CurveParameterDTO curveParameter,
            @NotNull GeneratorDTO generator) {
        super(ID);
        this.curveParameter = curveParameter;
        this.generator = generator;
    }

    @NotNull
    @Field
    private CurveParameterDTO curveParameter;

    @Field
    @NotNull
    private GeneratorDTO generator;

    public static GlobalPublicParameterDTO from(@NonNull GlobalPublicParameter gpp) {
        return new GlobalPublicParameterDTO(
                CurveParameterDTO.from(gpp.getPairingParameters()),
                GeneratorDTO.from(gpp.getG())
        );
    }

    public GlobalPublicParameter toGlobalPublicParameter(@NonNull PairingGenerator pairingGenerator) {
        PairingParameters pairingParameters = getCurveParameter().toPairingParameter();
        Pairing pairing = pairingGenerator.setupPairing(pairingParameters);
        Element g = getGenerator().toElement(pairing.getG1());
        return new GlobalPublicParameter(pairing, pairingParameters, g);
    }
}
