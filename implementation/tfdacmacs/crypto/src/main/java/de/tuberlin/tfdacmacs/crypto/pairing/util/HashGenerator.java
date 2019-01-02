package de.tuberlin.tfdacmacs.crypto.pairing.util;

import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Component
public class HashGenerator {
    private final MessageDigest sha256;
    private final MessageDigest md5;

    public HashGenerator() {
        try {
            this.sha256 = MessageDigest.getInstance("SHA-256");
            this.md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] sha256Hash(@NonNull String input) {
        return sha256Hash(input.getBytes(StandardCharsets.UTF_8));
    }

    public byte[] sha256Hash(byte[] input) {
        this.sha256.update(input);
        return this.sha256.digest();
    }

    public String fingerprint(byte[] input) {
        this.md5.update(input);
        byte[] digest = this.md5.digest();

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < digest.length-1; i++) {
            stringBuilder.append(Integer.toHexString(digest[i]));
            stringBuilder.append(':');
        }
        return stringBuilder.append(Integer.toHexString(digest[digest.length-1]))
                .toString();
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
