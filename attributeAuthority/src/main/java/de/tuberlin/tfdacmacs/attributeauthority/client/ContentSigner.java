package de.tuberlin.tfdacmacs.attributeauthority.client;

import de.tuberlin.tfdacmacs.attributeauthority.keypair.KeyPairService;
import de.tuberlin.tfdacmacs.crypto.rsa.StringAsymmetricCryptEngine;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;

@Component
@RequiredArgsConstructor
public class ContentSigner {

    private final StringAsymmetricCryptEngine stringAsymmetricCryptEngine;
    private final KeyPairService keyPairService;

    public String sign(@NonNull String content) {
        try {
            return stringAsymmetricCryptEngine.sign(content, keyPairService.getPrivateKey());
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
