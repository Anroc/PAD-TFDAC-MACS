package de.tuberlin.tfdacmacs.centralserver.gpp;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.basics.gpp.data.dto.CurveParameterDTO;
import de.tuberlin.tfdacmacs.basics.gpp.data.dto.GeneratorDTO;
import de.tuberlin.tfdacmacs.basics.gpp.data.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.basics.gpp.data.dto.RSAPublicKeyDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/gpp")
public class GlobalPublicParameterController {

    private final GlobalPublicParameterService gppService;

    @GetMapping
    public GlobalPublicParameterDTO getGlobalPublicParameter() {
        GlobalPublicParameter globalPublicParameter = gppService.createOrGetGPP();

        CurveParameterDTO curveParameterDTO = CurveParameterDTO.from(globalPublicParameter.getPairingParameters());
        GeneratorDTO generatorDTO = GeneratorDTO.from(globalPublicParameter.getG());
        RSAPublicKeyDTO rsaPublicKeyDTO = RSAPublicKeyDTO.from(globalPublicParameter.getRsaPublicKey());

        return new GlobalPublicParameterDTO(curveParameterDTO, generatorDTO, rsaPublicKeyDTO);
    }

}
