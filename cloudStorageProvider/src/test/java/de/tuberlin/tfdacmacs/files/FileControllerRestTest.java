package de.tuberlin.tfdacmacs.files;

import de.tuberlin.tfdacmacs.RestTestSuite;
import de.tuberlin.tfdacmacs.csp.files.data.FileInformation;
import de.tuberlin.tfdacmacs.csp.files.data.dto.FileInformationResponse;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class FileControllerRestTest extends RestTestSuite {

    private String id;
    private FileInformation fileInformation;
    private byte[] content = "content".getBytes();

    @Before
    public void setup() throws IOException {
        id = "myTestFile.dat";

        fileInformation = new FileInformation(
                id, "./data/" + id, id
        );

        fileInformationDB.insert(fileInformation);
        Path file = Files.createFile(Paths.get(fileInformation.getPath()));
        Files.write(file, content);
    }

    @After
    public void deleteFile() throws IOException {
        Files.deleteIfExists(Paths.get(fileInformation.getPath()));
    }

    @AfterClass
    public static void deleteDir() throws IOException {
        FileUtils.cleanDirectory(Paths.get("./data").toFile());
    }

    @Test
    public void createFile() throws Exception {
        String fileName = "obvious_p0rn";
        byte[] content = "only over 18!!!".getBytes();

        FileInformationResponse fileInformationResponse = createFile(fileName, content, null);

        String id = fileInformationResponse.getId();
        assertThat(id).isNotBlank();

        FileInformation fileInformation = fileInformationDB.findEntity(id).get();
        assertThat(fileInformation.getId()).isEqualTo(id);
        assertThat(fileInformation.getOriginalName()).isEqualTo(fileName);
        String path = fileInformation.getPath();
        assertThat(path).isNotBlank();

        byte[] bytes = Files.readAllBytes(Paths.get(path));
        assertSameElements(bytes, content);
    }

    @Test
    public void createFile_usingCustomId() throws Exception {
        String fileName = "obvious_p0rn";
        byte[] content = "only over 18!!!".getBytes();
        String id = UUID.randomUUID().toString();

        FileInformationResponse fileInformationResponse = createFile(fileName, content, id);

        String receivedId = fileInformationResponse.getId();
        assertThat(receivedId).isNotBlank().isEqualTo(id);

        FileInformation fileInformation = fileInformationDB.findEntity(receivedId).get();
        assertThat(fileInformation.getId()).isEqualTo(receivedId);
        assertThat(fileInformation.getOriginalName()).isEqualTo(fileName);
        String path = fileInformation.getPath();
        assertThat(path).isNotBlank();

        byte[] bytes = Files.readAllBytes(Paths.get(path));
        assertSameElements(bytes, content);
    }

    private FileInformationResponse createFile(String fileName, byte[] content, String id) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("name", fileName);
        map.add("filename", fileName);
        ByteArrayResource contentsAsResource = new ByteArrayResource(content){
            @Override
            public String getFilename(){
                return fileName;
            }
        };
        map.add("file", contentsAsResource);

        ResponseEntity<FileInformationResponse> exchange = mutualAuthRestTemplate.exchange(
                (id == null)? "/files" : String.format("/files?id=%s", id),
                HttpMethod.POST,
                new HttpEntity<>(map, httpHeaders),
                FileInformationResponse.class
        );

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.CREATED);
        return exchange.getBody();
    }

    @Test
    public void getFile() {
        ResponseEntity<byte[]> responseEntity = mutualAuthRestTemplate
                .getForEntity("/files/" + fileInformation.getId(), byte[].class);

        assertThat(responseEntity.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        System.out.println(new String(responseEntity.getBody()));
        assertSameElements(responseEntity.getBody(), content);
    }

    @Test
    public void getFileInformation() {
        ResponseEntity<FileInformationResponse> responseEntity = mutualAuthRestTemplate
                .getForEntity("/files/" + fileInformation.getId() + "/information", FileInformationResponse.class);
        assertThat(responseEntity.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getId()).isEqualTo(fileInformation.getId());
        assertThat(responseEntity.getBody().getName()).isEqualTo(fileInformation.getOriginalName());
    }
}
