package de.tuberlin.tfdacmacs.client.db.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;

import java.io.IOException;
import java.security.PublicKey;

public class PublicKeyDeserializer extends StdDeserializer<PublicKey> {

    public PublicKeyDeserializer() {
        super(PublicKey.class);
    }

    @Override
    public PublicKey deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        String base64 = node.asText();
        return KeyConverter.from(base64).toPublicKey();
    }
}

