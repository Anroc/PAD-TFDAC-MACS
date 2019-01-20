package de.tuberlin.tfdacmacs.files;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tuberlin.tfdacmacs.RestTestSuite;
import de.tuberlin.tfdacmacs.csp.files.data.FileInformation;
import de.tuberlin.tfdacmacs.csp.files.data.dto.FileInformationResponse;
import org.apache.http.entity.ContentType;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FileControllerRestTest extends RestTestSuite {

    @Autowired
    private WebApplicationContext webApplicationContext;

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

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        MockMultipartFile firstFile = new MockMultipartFile("file", fileName, ContentType.APPLICATION_OCTET_STREAM.toString(), content);
        MockMultipartHttpServletRequestBuilder file = MockMvcRequestBuilders
                .multipart(sslRestTemplate.getRootUri() + "/files")
                .file(firstFile);
        MvcResult mvcResult = mockMvc.perform(file)
                .andExpect(status().is(201))
                .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        FileInformationResponse fileInformationResponse = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(), FileInformationResponse.class);

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

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        MockMultipartFile firstFile = new MockMultipartFile("file", fileName, ContentType.APPLICATION_OCTET_STREAM.toString(), content);
        MockMultipartHttpServletRequestBuilder file = MockMvcRequestBuilders
                .multipart(sslRestTemplate.getRootUri() + "/files?id=" + id)
                .file(firstFile);
        MvcResult mvcResult = mockMvc.perform(file)
                .andExpect(status().is(201))
                .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        FileInformationResponse fileInformationResponse = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(), FileInformationResponse.class);

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

    @Test
    public void getFile() {
        ResponseEntity<byte[]> responseEntity = sslRestTemplate.getForEntity("/files/" + fileInformation.getId(), byte[].class);

        assertThat(responseEntity.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        System.out.println(new String(responseEntity.getBody()));
        assertSameElements(responseEntity.getBody(), content);
    }

    @Test
    public void getFileInformation() {
        ResponseEntity<FileInformationResponse> responseEntity = sslRestTemplate.getForEntity("/files/" + fileInformation.getId() + "/information", FileInformationResponse.class);
        assertThat(responseEntity.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getId()).isEqualTo(fileInformation.getId());
    }
}
