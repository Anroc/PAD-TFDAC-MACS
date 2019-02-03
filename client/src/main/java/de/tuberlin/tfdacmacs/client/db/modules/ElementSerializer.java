package de.tuberlin.tfdacmacs.client.db.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import it.unisa.dia.gas.jpbc.Element;

import java.io.IOException;

public class ElementSerializer extends StdSerializer<Element> {

    public ElementSerializer() {
        super(Element.class);
    }

    @Override
    public void serialize(Element value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(ElementConverter.convert(value));
    }
}
