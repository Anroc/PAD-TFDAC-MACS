package de.tuberlin.tfdacmacs.basics.factory;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.PairingGenerator;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.basics.crypto.rsa.AsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.basics.gpp.data.dto.CurveParameterDTO;
import de.tuberlin.tfdacmacs.basics.gpp.data.dto.GeneratorDTO;
import de.tuberlin.tfdacmacs.basics.gpp.data.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.basics.gpp.data.dto.RSAPublicKeyDTO;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GPPTestFactory {

    private final PairingGenerator pairingGenerator;
    private final AsymmetricCryptEngine cryptEngine;

    public GlobalPublicParameterDTO createDTO() {
        GlobalPublicParameter globalPublicParameter = create();

        CurveParameterDTO curveParameterDTO = CurveParameterDTO.from(globalPublicParameter.getPairingParameters());
        GeneratorDTO generatorDTO = GeneratorDTO.from(globalPublicParameter.getG());
        RSAPublicKeyDTO rsaPublicKeyDTO = RSAPublicKeyDTO.from(globalPublicParameter.getRsaPublicKey());

        return new GlobalPublicParameterDTO(curveParameterDTO, generatorDTO, rsaPublicKeyDTO);
    }

    public GlobalPublicParameter create() {
        PairingParameters pairingParameters = pairingGenerator.generateNewTypeACurveParameter();
        Pairing pairing = pairingGenerator.setupPairing(pairingParameters);
        GlobalPublicParameter globalPublicParameter = new GlobalPublicParameter(
                pairing, pairingParameters, pairing.getG1().newRandomElement().getImmutable(), cryptEngine.getPublicKey());
        return globalPublicParameter;
    }
}
