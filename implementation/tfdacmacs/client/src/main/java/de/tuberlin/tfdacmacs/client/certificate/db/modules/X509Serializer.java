package de.tuberlin.tfdacmacs.client.certificate.db.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;

import java.io.IOException;
import java.security.cert.X509Certificate;

public class X509Serializer extends StdSerializer<X509Certificate> {

    public X509Serializer() {
        super(X509Certificate.class);
    }

    @Override
    public void serialize(X509Certificate value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(KeyConverter.from(value).toBase64());
    }
}
