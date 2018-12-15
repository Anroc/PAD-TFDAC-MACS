package de.tuberlin.tfdacmacs.basics.crypto.pairing.util;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.GlobalPublicParameter;
import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
        this.messageDigest.update(input.getBytes(StandardCharsets.UTF_8));
        return this.messageDigest.digest();
    }

    public Element g1Hash(@NonNull GlobalPublicParameter gpp, @NonNull String input) {
        byte[] bytes = this.sha256Hash(input);
        return gpp.getPairing().getG1().newElementFromHash(bytes, 0, bytes.length);
    }
}
