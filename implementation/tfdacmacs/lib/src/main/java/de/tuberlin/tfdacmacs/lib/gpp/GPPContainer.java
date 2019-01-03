package de.tuberlin.tfdacmacs.lib.gpp;

import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.lib.gpp.events.GlobalPublicParameterChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class GPPContainer implements GlobalPublicParameterProvider {

    private GlobalPublicParameter globalPublicParameter;

    @EventListener(GlobalPublicParameterChangedEvent.class)
    public void updateGlobalPublicParameter(GlobalPublicParameterChangedEvent event) {
        this.globalPublicParameter = event.getSource();
    }

    @Override
    public GlobalPublicParameter getGlobalPublicParameter() {
        if(globalPublicParameter == null) {
            throw new IllegalStateException("GlobalPublicParameter accessed before they where retrieved!");
        }
        return this.globalPublicParameter;
    }
}
