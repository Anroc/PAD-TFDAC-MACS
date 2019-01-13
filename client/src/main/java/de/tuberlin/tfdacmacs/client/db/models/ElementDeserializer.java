package de.tuberlin.tfdacmacs.client.db.models;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;

import java.io.IOException;

public class ElementDeserializer extends StdDeserializer<Element> {

    private final Field field;

    public ElementDeserializer(Field field) {
        this(null, field);
    }

    public ElementDeserializer(Class<?> vc, Field field) {
        super(vc);
        this.field = field;
    }

    @Override
    public Element deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        String base64 = node.asText();
        return ElementConverter.convert(base64, field);
    }
}
