package de.tuberlin.tfdacmacs.basics.gpp;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.basics.gpp.events.GlobalPublicParameterChangedEvent;
import lombok.Data;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Data
@Component
public class GPPContainer implements GlobalPublicParameterProvider {

    private GlobalPublicParameter globalPublicParameter;

    @EventListener(GlobalPublicParameterChangedEvent.class)
    public void updateGlobalPublicParameter(GlobalPublicParameterChangedEvent event) {
        this.globalPublicParameter = event.getSource();
    }
}
