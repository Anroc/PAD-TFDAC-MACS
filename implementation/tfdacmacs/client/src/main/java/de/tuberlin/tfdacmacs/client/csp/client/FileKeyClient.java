package de.tuberlin.tfdacmacs.client.csp.client;

import de.tuberlin.tfdacmacs.client.csp.data.dto.CipherTextDTO;
import de.tuberlin.tfdacmacs.client.encrypt.data.EncryptedFile;
import de.tuberlin.tfdacmacs.client.rest.CSPClient;
import de.tuberlin.tfdacmacs.crypto.pairing.data.CipherText;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FileKeyClient {

    private final CSPClient cspClient;

    public void bulkCreateCipherText(@NonNull List<CipherText> cipherTexts) {
        cipherTexts.forEach(this::createCipherText);
    }

    public void createCipherText(@NonNull CipherText cipherText) {
        cspClient.createCipherText(CipherTextDTO.from(cipherText));
    }

    public void createFile(@NonNull EncryptedFile file) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("name", file.getFileName());
        map.add("filename", file.getFileName());
        ByteArrayResource contentsAsResource = new ByteArrayResource(file.getData()){
            @Override
            public String getFilename(){
                return file.getFileName();
            }
        };
        map.add("file", contentsAsResource);

        cspClient.withHeaders(httpHeaders).createFile(file.getId(), map);
    }
}
