package de.tuberlin.tfdacmacs.client.attribute.db;

import de.tuberlin.tfdacmacs.client.attribute.data.Attribute;
import de.tuberlin.tfdacmacs.client.db.JsonDB;
import de.tuberlin.tfdacmacs.client.gpp.events.GPPReceivedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static de.tuberlin.tfdacmacs.client.db.ModelFactory.elementModule;

@Component
public class AttributeDB extends JsonDB<Attribute> {

    public AttributeDB() {
        super(Attribute.class);
    }

    @EventListener(GPPReceivedEvent.class)
    public void initElementModule(GPPReceivedEvent gppReceivedEvent) {
        getFileEngine().registerModule(
                elementModule(gppReceivedEvent.getGPP().g1())
        );
    }
}
