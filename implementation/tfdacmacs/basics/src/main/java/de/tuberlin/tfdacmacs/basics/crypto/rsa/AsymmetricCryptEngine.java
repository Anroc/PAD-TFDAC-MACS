package de.tuberlin.tfdacmacs.basics.crypto.rsa;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.*;

/**
 * Shared component for encryption and decryption of messages.
 */
@Slf4j
public abstract class AsymmetricCryptEngine<T> extends CypherProcessor {

    public static final String ALGORITHM = "RSA";
    public static final int DEFAULT_BIT_SECURITY = 1024;
    public static final String CHAR_ENCODING = "UTF-8";
    private final int bitSecurity;
    @Setter
    private KeyPair asymmetricCipherKeyPair;

    public AsymmetricCryptEngine() {
        this(DEFAULT_BIT_SECURITY);
    }

    public AsymmetricCryptEngine(int bitSecurity) {
        this.bitSecurity = bitSecurity;
        Security.addProvider(new BouncyCastleProvider());
    }

    public KeyPair getAsymmetricCipherKeyPair() {
        if (asymmetricCipherKeyPair == null) {
            this.asymmetricCipherKeyPair = generateKeyPair();
        }

        return this.asymmetricCipherKeyPair;
    }

    /**
     * Generates a new RSA key pair.
     *
     * @return a new Key Pair
     */
    public KeyPair generateKeyPair() {
        KeyPairGenerator keyGen;
        try {
            keyGen = KeyPairGenerator.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        keyGen.initialize(this.bitSecurity);
        return keyGen.generateKeyPair();
    }

    public PublicKey getPublicKey() {
        return getAsymmetricCipherKeyPair().getPublic();
    }

    public PrivateKey getPrivateKey() {
        return getAsymmetricCipherKeyPair().getPrivate();
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
     * Returns a mac of the given data.
     * To do so this method will call {@link #getSHA256Hash(Object)} to create a SHA-256
     * hash of the given message. Then this hash will be digitally signed with the private key.
     *
     * @param data the data that shell be singed
     * @return the base64 encoded MAC
     * @throws BadPaddingException       on padding mismatch
     * @throws InvalidKeyException       on wrong cipher instance
     * @throws IllegalBlockSizeException on wrong alignment
     */
    abstract public String sign(T data)
            throws IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException;

    /**
     * Calculates the MD5 hash of the given data.
     *
     * @param data the data that shell be hashed
     * @return the base64 encoded hash
     */
    abstract public String getSHA256Hash(T data);

    /**
     * Verifies a MAC for the given plain string and with the given public key.
     *
     * @param mac       the mac that shell be verified
     * @param data      the plain message backed by the mac
     * @param publicKey the public key that created the mac
     * @return true if the signature is verified, else false
     */
    abstract public boolean isSignatureAuthentic(String mac, T data,
            PublicKey publicKey);

    public String getAlgorithm() {
        return ALGORITHM;
    }
}
