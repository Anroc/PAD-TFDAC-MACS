package de.tuberlin.tfdacmacs.crypto.pairing.util;

import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Component
public class HashGenerator {
    private final MessageDigest messageDigest;

    public HashGenerator() {
        try {
            this.messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] sha256Hash(@NonNull String input) {
        return sha256Hash(input.getBytes(StandardCharsets.UTF_8));
    }

    public byte[] sha256Hash(byte[] input) {
        this.messageDigest.update(input);
        return this.messageDigest.digest();
    }

    public String sha256HashWithSimpleEncoding(byte[] input) {
        this.messageDigest.update(input);
        return String.format("%040x", new BigInteger(1, this.messageDigest.digest()));
    }

    public byte[] sha256Hash(byte[] input, int outputLength) {
        byte[] hash = sha256Hash(input);
        return Arrays.copyOf(hash, outputLength);
    }

    public Element g1Hash(@NonNull GlobalPublicParameter gpp, @NonNull String input) {
        byte[] bytes = this.sha256Hash(input);
        return gpp.getPairing().getG1().newElementFromHash(bytes, 0, bytes.length);
    }
}
