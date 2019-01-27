package de.tuberlin.tfdacmacs.attributeauthority.gpp.client;

import de.tuberlin.tfdacmacs.attributeauthority.client.CAClient;
import de.tuberlin.tfdacmacs.crypto.pairing.PairingGenerator;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GPPClient {

    private final CAClient CAClient;
    private final PairingGenerator pairingGenerator;

    public GlobalPublicParameter getGPP() {
        return CAClient.getGPP().toGlobalPublicParameter(pairingGenerator);
    }
}
