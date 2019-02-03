package de.tuberlin.tfdacmacs.client.db.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;

import java.io.IOException;
import java.security.PrivateKey;

public class PrivateKeySerializer extends StdSerializer<PrivateKey> {

    public PrivateKeySerializer() {
        super(PrivateKey.class);
    }

    @Override
    public void serialize(PrivateKey value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(KeyConverter.from(value).toBase64());
    }
}
