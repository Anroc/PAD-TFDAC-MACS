package de.tuberlin.tfdacmacs.client.keypair.db;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.tuberlin.tfdacmacs.client.db.JsonDB;
import de.tuberlin.tfdacmacs.client.keypair.db.modules.PrivateKeyDeserializer;
import de.tuberlin.tfdacmacs.client.keypair.db.modules.PrivateKeySerializer;
import de.tuberlin.tfdacmacs.client.keypair.db.modules.PublicKeyDeserializer;
import de.tuberlin.tfdacmacs.client.keypair.db.modules.PublicKeySerializer;
import org.springframework.stereotype.Component;

import de.tuberlin.tfdacmacs.client.keypair.data.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

@Component
public class KeyPairDB extends JsonDB<KeyPair> {
    public KeyPairDB() {
        super(KeyPair.class);
    }

    @Override
    public Module[] getCustomModule() {
        return new Module[] {
                privateKeyModule(), publicKeyModule()
        };
    }

    private Module privateKeyModule() {
        SimpleModule privateKeyModel = new SimpleModule();
        privateKeyModel.addSerializer(PrivateKey.class, new PrivateKeySerializer());
        privateKeyModel.addDeserializer(PrivateKey.class, new PrivateKeyDeserializer());
        return privateKeyModel;
    }

    public Module publicKeyModule() {
        SimpleModule publicKeyModel = new SimpleModule();
        publicKeyModel.addSerializer(PublicKey.class, new PublicKeySerializer());
        publicKeyModel.addDeserializer(PublicKey.class, new PublicKeyDeserializer());
        return publicKeyModel;
    }
}
