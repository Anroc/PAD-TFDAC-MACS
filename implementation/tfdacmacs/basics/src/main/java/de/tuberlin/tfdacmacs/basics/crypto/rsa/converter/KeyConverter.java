package de.tuberlin.tfdacmacs.basics.crypto.rsa.converter;

import de.tuberlin.tfdacmacs.basics.crypto.rsa.AsymmetricCryptEngine;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.util.encoders.Base64;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeyConverter {

    public static KeyProducer from(String base64) {
        return new KeyProducer(Base64.decode(base64));
    }

    public static KeyProducer from(Key key) {
        return new KeyProducer(key.getEncoded());
    }

    public static KeyProducer from(byte[] key) {
        return new KeyProducer(key);
    }

    @RequiredArgsConstructor
    public static class KeyProducer {

        private final byte[] key;

        public PublicKey toPublicKey() {
            try {
                return KeyFactory.getInstance(AsymmetricCryptEngine.ALGORITHM)
                        .generatePublic(new X509EncodedKeySpec(key));
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        public String toBase64() {
            return new String(Base64.encode(key));
        }

        public PrivateKey toPrivateKey() {
            try {
                KeyFactory kf = KeyFactory
                        .getInstance("RSA"); // or "EC" or whatever
                return kf.generatePrivate(new PKCS8EncodedKeySpec(key));
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }
        }

        public byte[] toByes() {
            return key;
        }
    }
}
