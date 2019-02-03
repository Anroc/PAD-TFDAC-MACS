package de.tuberlin.tfdacmacs.client.db.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;

import java.io.IOException;
import java.security.PublicKey;

public class PublicKeySerializer extends StdSerializer<PublicKey> {

    public PublicKeySerializer() {
        super(PublicKey.class);
    }

    @Override
    public void serialize(PublicKey value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(KeyConverter.from(value).toBase64());
    }
}
