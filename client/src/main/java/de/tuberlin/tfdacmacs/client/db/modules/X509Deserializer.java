package de.tuberlin.tfdacmacs.client.db.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;

import java.io.IOException;
import java.security.cert.X509Certificate;

public class X509Deserializer extends StdDeserializer<X509Certificate> {

    public X509Deserializer() {
        super(X509Certificate.class);
    }

    @Override
    public X509Certificate deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        String base64 = node.asText();
        return KeyConverter.from(base64).toX509Certificate();
    }
}
