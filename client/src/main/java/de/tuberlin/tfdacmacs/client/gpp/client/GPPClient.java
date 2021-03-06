package de.tuberlin.tfdacmacs.client.gpp.client;

import de.tuberlin.tfdacmacs.client.gpp.client.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.client.gpp.events.GPPReceivedEvent;
import de.tuberlin.tfdacmacs.client.rest.CAClient;
import de.tuberlin.tfdacmacs.crypto.pairing.PairingGenerator;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GPPClient {

    private final CAClient caClient;
    private final PairingGenerator pairingGenerator;
    private final ApplicationEventPublisher publisher;

    public GlobalPublicParameter getGPP() {
        GlobalPublicParameterDTO globalPublicParamterDTO = caClient.getGPP();
        GlobalPublicParameter gpp = globalPublicParamterDTO.toGlobalPublicParameter(pairingGenerator);
        publisher.publishEvent(new GPPReceivedEvent(gpp));
        return gpp;
    }
}
