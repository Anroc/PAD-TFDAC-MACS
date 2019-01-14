package de.tuberlin.tfdacmacs.client.gpp.factory;

import de.tuberlin.tfdacmacs.client.gpp.data.dto.CurveParameterDTO;
import de.tuberlin.tfdacmacs.client.gpp.data.dto.GeneratorDTO;
import de.tuberlin.tfdacmacs.client.gpp.data.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.PairingParameters;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class GPPDTOTestFactory {

    public GlobalPublicParameterDTO create(GlobalPublicParameter gpp) {
        return new GlobalPublicParameterDTO(
                from(gpp.getPairingParameters()),
                from(gpp.getG())
        );
    }

    public CurveParameterDTO from(@NonNull PairingParameters pairingParameters) {
        CurveParameterDTO curveParameterDTO = new CurveParameterDTO();
        curveParameterDTO.setQ(pairingParameters.getString("q"));
        curveParameterDTO.setH(pairingParameters.getString("h"));
        curveParameterDTO.setR(pairingParameters.getString("r"));
        curveParameterDTO.setExp1(pairingParameters.getInt("exp1"));
        curveParameterDTO.setExp2(pairingParameters.getInt("exp2"));
        curveParameterDTO.setSign0(pairingParameters.getInt("sign0"));
        curveParameterDTO.setSign1(pairingParameters.getInt("sign1"));
        return curveParameterDTO;
    }


    public GeneratorDTO from(@NonNull Element g) {
        GeneratorDTO generatorDTO = new GeneratorDTO();
        generatorDTO.setG(ElementConverter.convert(g));
        return generatorDTO;
    }
}
