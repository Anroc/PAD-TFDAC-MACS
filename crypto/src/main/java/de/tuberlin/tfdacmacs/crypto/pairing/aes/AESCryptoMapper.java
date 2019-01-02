package de.tuberlin.tfdacmacs.crypto.pairing.aes;

import de.tuberlin.tfdacmacs.crypto.pairing.util.HashGenerator;
import de.tuberlin.tfdacmacs.crypto.rsa.StringSymmetricCryptEngine;
import it.unisa.dia.gas.jpbc.Element;
import lombok.RequiredArgsConstructor;

import java.security.Key;

@RequiredArgsConstructor
public abstract class AESCryptoMapper {

    protected final HashGenerator hashGenerator;
    protected final StringSymmetricCryptEngine symmetricCryptEngine;

    protected Key generateAesKey(Element key) {
        byte[] randomBytes = hashGenerator.sha256Hash(key.toBytes(), 32);
        return symmetricCryptEngine.createKeyFromBytes(randomBytes);
    }

}
