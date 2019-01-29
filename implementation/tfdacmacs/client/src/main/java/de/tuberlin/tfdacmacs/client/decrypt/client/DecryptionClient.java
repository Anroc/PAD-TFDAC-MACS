package de.tuberlin.tfdacmacs.client.decrypt.client;

import de.tuberlin.tfdacmacs.client.csp.data.dto.FileInformationResponse;
import de.tuberlin.tfdacmacs.client.encrypt.data.EncryptedFile;
import de.tuberlin.tfdacmacs.client.rest.CSPClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DecryptionClient {

    private final CSPClient cspClient;

    public EncryptedFile getFile(@NonNull String fileId) {
        byte[] file = cspClient.getFile(fileId);
        FileInformationResponse fileInfo = cspClient.getFileInformation(fileId);

        return new EncryptedFile(fileInfo.getId(), fileInfo.getName(), file);
    }
}
