package de.tuberlin.tfdacmacs.client.csp;

import de.tuberlin.tfdacmacs.client.attribute.data.Attribute;
import de.tuberlin.tfdacmacs.client.csp.client.FileKeyClient;
import de.tuberlin.tfdacmacs.client.encrypt.data.EncryptedFile;
import de.tuberlin.tfdacmacs.crypto.pairing.data.CipherText;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public List<CipherText> checkForDecryptableFiles(@NonNull Set<Attribute> attributes) {
        return fileKeyClient.getCipherTexts(
                    attributes.stream()
                            .map(Attribute::getId)
                            .collect(Collectors.toList()));
    }
}
