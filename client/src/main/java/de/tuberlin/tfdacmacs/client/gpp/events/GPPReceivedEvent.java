package de.tuberlin.tfdacmacs.client.gpp.events;

import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import org.springframework.context.ApplicationEvent;

public class GPPReceivedEvent extends ApplicationEvent {

    public GPPReceivedEvent(GlobalPublicParameter source) {
        super(source);
    }

    public GlobalPublicParameter getGPP() {
        return (GlobalPublicParameter) source;
    }
}
