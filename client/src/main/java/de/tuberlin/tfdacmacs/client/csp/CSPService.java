package de.tuberlin.tfdacmacs.client.csp;

import de.tuberlin.tfdacmacs.client.csp.client.FileKeyClient;
import de.tuberlin.tfdacmacs.client.encrypt.data.EncryptedFile;
import de.tuberlin.tfdacmacs.crypto.pairing.data.CipherText;
import de.tuberlin.tfdacmacs.crypto.pairing.data.File;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CSPService {

    private final FileKeyClient fileKeyClient;

    public void createCipherTexts(@NonNull List<CipherText> cipherTexts) {
        fileKeyClient.bulkCreateCipherText(cipherTexts);
    }

    public void uploadFile(@NonNull EncryptedFile file) {
        fileKeyClient.createFile(file);
    }
}
