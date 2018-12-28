package de.tuberlin.tfdacmacs.attributeauthority.init.gpp.client;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.PairingGenerator;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.GlobalPublicParameter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GPPClient {

    private final GPPFeignClient gppFeignClient;
    private final PairingGenerator pairingGenerator;

    public GlobalPublicParameter getGPP() {
        return gppFeignClient.getGPP().toGlobalPublicParameter(pairingGenerator);
    }
}
