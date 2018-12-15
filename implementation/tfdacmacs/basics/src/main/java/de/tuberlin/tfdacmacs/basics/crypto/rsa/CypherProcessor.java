package de.tuberlin.tfdacmacs.basics.crypto.rsa;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public abstract class CypherProcessor {

    abstract public String getAlgorithm();

    protected byte[] process(byte[] data, Key key, int mode)
            throws InvalidKeyException, BadPaddingException,
            IllegalBlockSizeException {
        try {
            Cipher cipher = Cipher.getInstance(getAlgorithm());
            cipher.init(mode, key);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }
}
