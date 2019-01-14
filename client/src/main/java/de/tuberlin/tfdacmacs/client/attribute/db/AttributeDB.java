package de.tuberlin.tfdacmacs.client.attribute.db;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.tuberlin.tfdacmacs.client.attribute.data.Attribute;
import de.tuberlin.tfdacmacs.client.db.JsonDB;
import de.tuberlin.tfdacmacs.client.attribute.db.modules.ElementDeserializer;
import de.tuberlin.tfdacmacs.client.attribute.db.modules.ElementSerializer;
import de.tuberlin.tfdacmacs.client.gpp.events.GPPReceivedEvent;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AttributeDB extends JsonDB<Attribute> {

    public AttributeDB() {
        super(Attribute.class);
    }

    @EventListener(GPPReceivedEvent.class)
    public void initElementModule(GPPReceivedEvent gppReceivedEvent) {
        getFileEngine().registerModule(elementModule(gppReceivedEvent.getGPP().getPairing().getG1()));
    }

    private Module elementModule(Field field) {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Element.class, new ElementDeserializer(field));
        module.addSerializer(Element.class, new ElementSerializer());
        return module;
    }
}
