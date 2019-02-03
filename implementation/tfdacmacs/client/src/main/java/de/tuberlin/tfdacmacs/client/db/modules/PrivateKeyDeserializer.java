package de.tuberlin.tfdacmacs.client.db.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;

import java.io.IOException;
import java.security.PrivateKey;

public class PrivateKeyDeserializer extends StdDeserializer<PrivateKey> {

    public PrivateKeyDeserializer() {
        super(PrivateKey.class);
    }

    @Override
    public PrivateKey deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        String base64 = node.asText();
        return KeyConverter.from(base64).toPrivateKey();
    }
}
