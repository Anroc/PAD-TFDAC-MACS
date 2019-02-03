package de.tuberlin.tfdacmacs.client.keypair.db;

import com.fasterxml.jackson.databind.Module;
import de.tuberlin.tfdacmacs.client.db.JsonDB;
import de.tuberlin.tfdacmacs.client.keypair.data.KeyPair;
import org.springframework.stereotype.Component;

import static de.tuberlin.tfdacmacs.client.db.ModelFactory.privateKeyModule;
import static de.tuberlin.tfdacmacs.client.db.ModelFactory.publicKeyModule;

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
}
