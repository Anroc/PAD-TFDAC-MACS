package de.tuberlin.tfdacmacs.crypto.rsa.data;

import lombok.Data;

@Data
public class EncryptedFile {

    private final String fileKey;
    private final byte[] data;
}
