package de.tuberlin.tfdacmacs.csp.gpp;

import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.csp.config.CSPConfig;
import de.tuberlin.tfdacmacs.csp.gpp.client.GPPClient;
import de.tuberlin.tfdacmacs.lib.gpp.events.GlobalPublicParameterChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GPPService {

    private final GPPClient gppClient;
    private final CSPConfig config;
    private final ApplicationEventPublisher eventPublisher;

    private GlobalPublicParameter globalPublicParameter;

    @EventListener(ApplicationReadyEvent.class)
    public void initGPP() {
        if(config.isRequestCaOnInit()) {
            retrieveGPP();
        }
    }

    private GlobalPublicParameter retrieveGPP() {
        this.globalPublicParameter = gppClient.getGPP();
        eventPublisher.publishEvent(new GlobalPublicParameterChangedEvent(this.globalPublicParameter));
        return globalPublicParameter;
    }
}
