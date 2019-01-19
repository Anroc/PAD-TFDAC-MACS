package de.tuberlin.tfdacmacs.client.encrypt.data;

import de.tuberlin.tfdacmacs.crypto.pairing.data.File;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EncryptedFile {

    private String id;
    private String fileName;
    private byte[] data;

    public static EncryptedFile from(@NonNull File file, String fileName) {
        return new EncryptedFile(file.getId(), fileName, file.getData());
    }
}
