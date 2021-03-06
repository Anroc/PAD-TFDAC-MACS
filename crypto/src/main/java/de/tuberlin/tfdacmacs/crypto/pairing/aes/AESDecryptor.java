package de.tuberlin.tfdacmacs.crypto.pairing.aes;

import de.tuberlin.tfdacmacs.crypto.pairing.util.HashGenerator;
import de.tuberlin.tfdacmacs.crypto.rsa.StringSymmetricCryptEngine;
import it.unisa.dia.gas.jpbc.Element;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.security.Key;

@Slf4j
@Component
public class AESDecryptor extends AESCryptoMapper {

    @Autowired
    public AESDecryptor(HashGenerator hashGenerator,
            StringSymmetricCryptEngine symmetricCryptEngine) {
        super(hashGenerator, symmetricCryptEngine);
    }

    public byte[] decrypt(byte[] encryptedContent, Element key) {
        Key symmetricKey = generateAesKey(key);
        try {
            return symmetricCryptEngine.decryptRaw(encryptedContent, symmetricKey);
        } catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
            log.error("Decryption failed. Maybe wrong key is used.", e);
            return new byte[0];
        }
    }

}
