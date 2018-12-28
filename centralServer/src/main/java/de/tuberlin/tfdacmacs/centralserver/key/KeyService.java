package de.tuberlin.tfdacmacs.centralserver.key;

import de.tuberlin.tfdacmacs.basics.crypto.rsa.StringAsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.basics.exceptions.ServiceException;
import de.tuberlin.tfdacmacs.centralserver.key.data.RsaKeyPair;
import de.tuberlin.tfdacmacs.centralserver.key.db.KeyDB;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PublicKey;

@Service
@RequiredArgsConstructor
public class KeyService {

    private final KeyDB keyDB;
    private final StringAsymmetricCryptEngine cryptEngine;

    @PostConstruct
    public void saveKeys() {
        if(! keyDB.existRsaKeyPair()) {
            KeyPair keyPair = cryptEngine.getAsymmetricCipherKeyPair();
            keyDB.insert(new RsaKeyPair(keyPair.getPublic(), keyPair.getPrivate()));
        } else {
            RsaKeyPair rsaKeyPair = keyDB.findEntity().get();
            cryptEngine.setAsymmetricCipherKeyPair(rsaKeyPair.getKeyPair());
        }
    }

    public PublicKey getPublicKey() {
        return keyDB.findEntity().map(RsaKeyPair::getPublicKey).get();
    }

    public String sign(@NonNull String content) {
        try {
            return cryptEngine.sign(content);
        } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException e) {
            throw new ServiceException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
