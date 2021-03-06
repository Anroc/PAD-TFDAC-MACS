package de.tuberlin.tfdacmacs.client.gpp;

import de.tuberlin.tfdacmacs.client.gpp.client.GPPClient;
import de.tuberlin.tfdacmacs.client.register.events.LoginEvent;
import de.tuberlin.tfdacmacs.client.register.events.LogoutEvent;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GPPService {

    private final GPPClient gppClient;

    private GlobalPublicParameter gpp;

    @EventListener(LoginEvent.class)
    public GlobalPublicParameter getGPP() {
        if(this.gpp == null) {
            this.gpp = gppClient.getGPP();
        }

        return this.gpp;
    }

    @EventListener(LogoutEvent.class)
    public void cleanState() {
        this.gpp = null;
    }
}
