package de.tuberlin.tfdacmacs.client.twofactor.db;

import de.tuberlin.tfdacmacs.client.db.JsonDB;
import de.tuberlin.tfdacmacs.client.gpp.events.GPPReceivedEvent;
import de.tuberlin.tfdacmacs.client.twofactor.data.PublicTwoFactorAuthentication;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static de.tuberlin.tfdacmacs.client.db.ModelFactory.elementModule;

@Component
public class PublicTwoFactorAuthenticationDB extends JsonDB<PublicTwoFactorAuthentication> {
    public PublicTwoFactorAuthenticationDB() {
        super(PublicTwoFactorAuthentication.class);
    }

    @EventListener(GPPReceivedEvent.class)
    public void initElementModule(GPPReceivedEvent gppReceivedEvent) {
        getFileEngine().registerModule(
                elementModule(gppReceivedEvent.getGPP().getPairing().getG1())
        );
    }
}
