package de.tuberlin.tfdacmacs.client.db;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.tuberlin.tfdacmacs.client.db.modules.*;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import lombok.NonNull;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

public class ModelFactory {

    public static Module privateKeyModule() {
        return buildModel(PrivateKey.class, new PrivateKeySerializer(), new PrivateKeyDeserializer());
    }

    public static Module publicKeyModule() {
        return buildModel(PublicKey.class, new PublicKeySerializer(), new PublicKeyDeserializer());
    }

    public static Module elementModule(@NonNull Field field) {
        return buildModel(Element.class, new ElementSerializer(), new ElementDeserializer(field));
    }

    public static Module x509Module() {
        return buildModel(X509Certificate.class, new X509Serializer(), new X509Deserializer());
    }

    private static <T> Module buildModel(Class<T> clazz, StdSerializer<T> serializer, StdDeserializer<T> deserializer) {
        SimpleModule module = new SimpleModule();
        module.addSerializer(clazz, serializer);
        module.addDeserializer(clazz, deserializer);
        return module;
    }
}
