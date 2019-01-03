package de.tuberlin.tfdacmacs.crypto.rsa;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.*;

@Slf4j
public class StringAsymmetricCryptEngine extends AsymmetricCryptEngine<String> {

    public StringAsymmetricCryptEngine(int bitSecurity) {
        super(bitSecurity);
    }

    public StringAsymmetricCryptEngine() {
        super();
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
    @Override
    public String encrypt(String data, Key key)
            throws BadPaddingException, InvalidKeyException,
            IllegalBlockSizeException {
        try {
            return new String(
                    Base64.encode(process(data.getBytes(CHAR_ENCODING), key,
                            Cipher.ENCRYPT_MODE)),
                    Charset.forName(CHAR_ENCODING)
            );
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
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
    @Override
    public String decrypt(String data, Key key)
            throws BadPaddingException, InvalidKeyException,
            IllegalBlockSizeException {
        return new String(
                process(Base64.decode(data), key, Cipher.DECRYPT_MODE),
                Charset.forName(CHAR_ENCODING));
    }

    /**
     * Returns a mac of the given plain string.
     * To do so this method will call {@link #getSHA256Hash(String)} to create a SHA-256
     * hash of the given message. Then this hash will be digitally signed with the private key.
     *
     * @param data the plain string that shell be singed
     * @return the base64 encoded MAC
     * @throws BadPaddingException       on padding mismatch
     * @throws InvalidKeyException       on wrong cipher instance
     * @throws IllegalBlockSizeException on wrong alignment
     */
    @Override
    public String sign(String data)
            throws IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException {
        String hash = getSHA256Hash(data);

        return encrypt(hash, getPrivateKey());
    }

    /**
     * Calculates the MD5 hash of the given string.
     *
     * @param plain the string that shell be hashed
     * @return the base64 encoded hash
     */
    @Override
    public String getSHA256Hash(String plain) {
        try {
            return new String(
                    Base64.encode(MessageDigest.getInstance("SHA-256")
                            .digest(plain.getBytes(CHAR_ENCODING))),
                    Charset.forName(CHAR_ENCODING)
            );
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Verifies a MAC for the given plain string and with the given public key.
     *
     * @param mac       the mac that shell be verified
     * @param data      the plain message backed by the mac
     * @param publicKey the public key that created the mac
     * @return true if the signature is verified, else flase
     * @throws BadPaddingException       on padding mismatch
     * @throws InvalidKeyException       on wrong cipher instance
     * @throws IllegalBlockSizeException on wrong alignment
     */
    public boolean isSignatureAuthentic(String mac, String data,
            PublicKey publicKey) {
        String hash = getSHA256Hash(data);

        try {
            return decrypt(mac, publicKey).equals(hash);
        } catch (GeneralSecurityException e) {
            log.warn("Could not decrypt mac with public key.", e);
            return false;
        }
    }
}
