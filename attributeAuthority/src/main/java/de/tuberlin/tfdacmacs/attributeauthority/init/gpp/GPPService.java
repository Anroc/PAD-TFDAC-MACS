package de.tuberlin.tfdacmacs.attributeauthority.init.gpp;

import de.tuberlin.tfdacmacs.attributeauthority.config.AttributeAuthorityConfig;
import de.tuberlin.tfdacmacs.attributeauthority.init.gpp.client.GPPClient;
import de.tuberlin.tfdacmacs.attributeauthority.init.gpp.events.GPPReceivedEvent;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.GlobalPublicParameter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GPPService {

    private final GPPClient gppClient;
    private final AttributeAuthorityConfig config;
    private final ApplicationEventPublisher eventPublisher;

    private GlobalPublicParameter gpp;

    @EventListener(ApplicationReadyEvent.class)
    public void initGPP() {
        if(config.isRequestGPPOnInit()) {
            retrieveGPP();
        }
    }

    private GlobalPublicParameter retrieveGPP() {
        this.gpp = gppClient.getGPP();
        eventPublisher.publishEvent(new GPPReceivedEvent(this.gpp));
        return gpp;
    }

    public GlobalPublicParameter getGpp() {
        if (gpp == null) {
           this.gpp = retrieveGPP();
        }
        return gpp;
    }
}
