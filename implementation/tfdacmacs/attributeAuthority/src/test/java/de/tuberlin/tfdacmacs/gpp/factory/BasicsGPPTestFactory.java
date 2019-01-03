package de.tuberlin.tfdacmacs.gpp.factory;

import de.tuberlin.tfdacmacs.lib.gpp.data.dto.CurveParameterDTO;
import de.tuberlin.tfdacmacs.lib.gpp.data.dto.GeneratorDTO;
import de.tuberlin.tfdacmacs.lib.gpp.data.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.crypto.GPPTestFactory;
import de.tuberlin.tfdacmacs.crypto.pairing.PairingGenerator;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.crypto.rsa.StringAsymmetricCryptEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BasicsGPPTestFactory extends GPPTestFactory {

    @Autowired
    public BasicsGPPTestFactory(PairingGenerator pairingGenerator,
            StringAsymmetricCryptEngine cryptEngine) {
        super(pairingGenerator, cryptEngine);
    }

    public GlobalPublicParameterDTO createDTO() {
        GlobalPublicParameter globalPublicParameter = create();

        CurveParameterDTO curveParameterDTO = CurveParameterDTO.from(globalPublicParameter.getPairingParameters());
        GeneratorDTO generatorDTO = GeneratorDTO.from(globalPublicParameter.getG());

        return new GlobalPublicParameterDTO(curveParameterDTO, generatorDTO);
    }
}
