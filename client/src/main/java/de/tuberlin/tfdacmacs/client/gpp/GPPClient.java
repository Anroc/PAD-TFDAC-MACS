package de.tuberlin.tfdacmacs.client.gpp;

import de.tuberlin.tfdacmacs.client.gpp.data.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.client.rest.CaClient;
import de.tuberlin.tfdacmacs.crypto.pairing.PairingGenerator;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GPPClient {

    private final CaClient caClient;
    private final PairingGenerator pairingGenerator;

    public GlobalPublicParameter getGPP() {
        GlobalPublicParameterDTO globalPublicParamterDTO = caClient.getGPP();
        return globalPublicParamterDTO.toGlobalPublicParameter(pairingGenerator);
    }
}
