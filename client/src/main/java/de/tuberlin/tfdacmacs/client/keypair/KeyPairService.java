package de.tuberlin.tfdacmacs.client.keypair;

import de.tuberlin.tfdacmacs.client.keypair.db.KeyPairDB;
import de.tuberlin.tfdacmacs.crypto.rsa.AsymmetricCryptEngine;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import de.tuberlin.tfdacmacs.client.keypair.data.KeyPair;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class KeyPairService {

    private final AsymmetricCryptEngine<?> cryptEngine;
    private final KeyPairDB keyPairDB;

    public KeyPair getKeyPair(@NonNull String email) {
        Optional<KeyPair> optionalKeyPair = findKeyPair(email);
        if(optionalKeyPair.isPresent()) {
            return optionalKeyPair.get();
        } else {
            KeyPair keyPair = KeyPair.from(cryptEngine.generateKeyPair());
            saveKeyPair(email, keyPair);
            return keyPair;
        }
    }

    private void saveKeyPair(String email, KeyPair keyPair) {
        keyPairDB.insert(email, keyPair);
    }

    public Optional<KeyPair> findKeyPair(@NonNull String email) {
        return keyPairDB.find(email);
    }
}
