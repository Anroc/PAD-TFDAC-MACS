package de.tuberlin.tfdacmacs.csp.gpp.client;

import de.tuberlin.tfdacmacs.crypto.pairing.PairingGenerator;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.csp.client.CAClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GPPClient {

    private final CAClient caClient;
    private final PairingGenerator pairingGenerator;

    public GlobalPublicParameter getGPP() {
        return caClient.getGPP().toGlobalPublicParameter(pairingGenerator);
    }
}
