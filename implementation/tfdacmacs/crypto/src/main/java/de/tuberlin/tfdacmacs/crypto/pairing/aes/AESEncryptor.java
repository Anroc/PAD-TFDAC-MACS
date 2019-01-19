package de.tuberlin.tfdacmacs.crypto.pairing.aes;

import de.tuberlin.tfdacmacs.crypto.pairing.util.HashGenerator;
import de.tuberlin.tfdacmacs.crypto.rsa.StringSymmetricCryptEngine;
import it.unisa.dia.gas.jpbc.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.security.Key;

@Component
public class AESEncryptor extends AESCryptoMapper {

    @Autowired
    public AESEncryptor(HashGenerator hashGenerator,
            StringSymmetricCryptEngine symmetricCryptEngine) {
        super(hashGenerator, symmetricCryptEngine);
    }

    public byte[] encrypt(byte[] data, Element key) {
        Key symmetricKey = generateAesKey(key);
        try {
            return symmetricCryptEngine.encryptRaw(data, symmetricKey);
        } catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }
}
