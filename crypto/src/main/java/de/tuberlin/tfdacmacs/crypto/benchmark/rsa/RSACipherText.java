package de.tuberlin.tfdacmacs.crypto.benchmark.rsa;

import de.tuberlin.tfdacmacs.crypto.benchmark.CipherTextLength;
import lombok.Data;

import java.util.Map;

@Data
public class RSACipherText implements CipherTextLength {

    private final Map<String, EncryptedFile> encryptedFile;

    @Override
    public long getSize() {
        return encryptedFile.values().stream().mapToLong(this::getSize).sum();
    }

    @Override
    public long getNumberOfFileKeys() {
        return encryptedFile.size();
    }

    private long getSize(EncryptedFile encryptedFile) {
        return encryptedFile.getFileKey().getBytes().length + encryptedFile.getData().length;
    }
}
