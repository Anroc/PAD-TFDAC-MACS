package de.tuberlin.tfdacmacs.attributeauthority.keypair;

import de.tuberlin.tfdacmacs.crypto.rsa.certificate.JavaKeyStore;
import de.tuberlin.tfdacmacs.lib.config.KeyStoreConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.*;

@Service
@RequiredArgsConstructor
public class KeyPairService {

    private final JavaKeyStore javaKeyStore;
    private final KeyStoreConfig keyStoreConfig;

    public PrivateKey getPrivateKey() {
        try {
            return (PrivateKey) javaKeyStore.getKeyEntry(keyStoreConfig.getKeyAlias(), keyStoreConfig.getKeyPassword());
        } catch (UnrecoverableKeyException |NoSuchAlgorithmException |KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

}
