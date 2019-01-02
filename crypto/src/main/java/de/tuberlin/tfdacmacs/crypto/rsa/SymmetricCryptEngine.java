package de.tuberlin.tfdacmacs.crypto.rsa;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.*;

@Slf4j
public abstract class SymmetricCryptEngine<T> extends CypherProcessor {

    public static final String ALGORITHM = "AES";
    public static final int DEFAULT_BIT_SECURITY = 256;
    public static final String CHAR_ENCODING = "UTF-8";
    @Getter
    private final int bitSecurity;
    @Setter
    private Key symmetricCipherKey;

    public SymmetricCryptEngine() {
        this(DEFAULT_BIT_SECURITY);
    }

    public SymmetricCryptEngine(int bitSecurity) {
        this.bitSecurity = bitSecurity;
        Security.addProvider(new BouncyCastleProvider());
    }

    public Key getSymmetricCipherKey() {
        if (symmetricCipherKey == null) {
            this.symmetricCipherKey = generateKey();
        }

        return this.symmetricCipherKey;
    }

    /**
     * Generates a new RSA key pair.
     *
     * @return a new AuthorityKey Pair
     */
    protected Key generateKey() {
        KeyGenerator keyGen;
        try {
            keyGen = KeyGenerator.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        keyGen.init(this.bitSecurity, new SecureRandom());
        return keyGen.generateKey();
    }

    public Key createKeyFromBytes(byte[] randomData) {
        return new SecretKeySpec(randomData, ALGORITHM);
    }

    /**
     * Encrypts the given data with the given key.
     *
     * @param data the data that shell be encrypted
     * @param key  the key that will be used (either public or private)
     * @return the base64 encoded encrypted string.
     * @throws BadPaddingException       on padding mismatch
     * @throws InvalidKeyException       on wrong cipher instance
     * @throws IllegalBlockSizeException on wrong alignment
     */
    abstract public String encrypt(T data, Key key)
            throws BadPaddingException, InvalidKeyException,
            IllegalBlockSizeException;

    /**
     * Encrypts the given data with the given key.
     *
     * @param data the data that shell be encrypted
     * @param key  the key that will be used (either public or private)
     * @return the base64 encoded encrypted string.
     * @throws BadPaddingException       on padding mismatch
     * @throws InvalidKeyException       on wrong cipher instance
     * @throws IllegalBlockSizeException on wrong alignment
     */
    public String encryptRaw(byte[] data, Key key) throws BadPaddingException, InvalidKeyException,
            IllegalBlockSizeException {
        return new String(
                Base64.encode(process(data, key, Cipher.ENCRYPT_MODE)),
                Charset.forName(CHAR_ENCODING)
        );
    }

    /**
     * Decrypts the given data with the given key.
     *
     * @param data the base64 encoded data that shell be decrypted
     * @param key  the key that will be used (either public or private)
     * @return the plain string
     * @throws BadPaddingException       on padding mismatch
     * @throws InvalidKeyException       on wrong cipher instance
     * @throws IllegalBlockSizeException on wrong alignment
     */
    abstract public T decrypt(String data, Key key)
            throws BadPaddingException, InvalidKeyException,
            IllegalBlockSizeException;

    /**
     * Decrypts the given data with the given key.
     *
     * @param data the base64 encoded data that shell be decrypted
     * @param key  the key that will be used (either public or private)
     * @return the plain string
     * @throws BadPaddingException       on padding mismatch
     * @throws InvalidKeyException       on wrong cipher instance
     * @throws IllegalBlockSizeException on wrong alignment
     */
    public byte[] decryptRaw(String data, Key key)
            throws BadPaddingException, InvalidKeyException,
            IllegalBlockSizeException {
        return process(Base64.decode(data), key, Cipher.DECRYPT_MODE);
    }

    @Override
    public String getAlgorithm() {
        return ALGORITHM;
    }
}
