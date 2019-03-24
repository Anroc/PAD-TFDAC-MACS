package de.tuberlin.tfdacmacs.crypto.benchmark.rsa;

import lombok.Data;

@Data
public class EncryptedFile {

    private final String fileKey;
    private final byte[] data;
}
