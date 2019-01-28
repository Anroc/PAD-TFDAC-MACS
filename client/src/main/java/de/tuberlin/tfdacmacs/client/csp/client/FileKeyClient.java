package de.tuberlin.tfdacmacs.client.csp.client;

import de.tuberlin.tfdacmacs.client.csp.data.dto.CipherTextDTO;
import de.tuberlin.tfdacmacs.client.encrypt.data.EncryptedFile;
import de.tuberlin.tfdacmacs.client.gpp.GPPService;
import de.tuberlin.tfdacmacs.client.rest.CSPClient;
import de.tuberlin.tfdacmacs.crypto.pairing.data.CipherText;
import it.unisa.dia.gas.jpbc.Field;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FileKeyClient {

    private final CSPClient cspClient;
    private final GPPService gppService;

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

    public List<CipherText> getCipherTexts(List<String> attributeIds) {
        return cspClient.getCipherTexts(attributeIds)
                .stream()
                .map(ct -> ct.toCipherText(g1(), gt()))
                .collect(Collectors.toList());

    }

    private Field g1() {
        return gppService.getGPP().getPairing().getG1();
    }

    private Field gt() {
        return gppService.getGPP().getPairing().getGT();
    }
}
