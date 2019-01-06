package de.tuberlin.tfdacmacs.workflows;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.tuberlin.tfdacmacs.IntegrationTestSuite;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;
import de.tuberlin.tfdacmacs.dto.attributeauthority.attribute.AttributeCreationRequest;
import de.tuberlin.tfdacmacs.dto.attributeauthority.attribute.AttributeType;
import de.tuberlin.tfdacmacs.dto.attributeauthority.attribute.PublicAttributeResponse;
import de.tuberlin.tfdacmacs.dto.attributeauthority.user.AttributeValueRequest;
import de.tuberlin.tfdacmacs.dto.attributeauthority.user.CreateUserRequest;
import de.tuberlin.tfdacmacs.dto.attributeauthority.user.UserResponse;
import de.tuberlin.tfdacmacs.dto.centralauthority.certificate.CaCertificateResponse;
import de.tuberlin.tfdacmacs.dto.centralauthority.certificate.CertificateRequest;
import org.assertj.core.api.Java6Assertions;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;

import static org.assertj.core.api.Assertions.assertThat;

public class RegisterUser extends IntegrationTestSuite {

    private String attributeId;
    private String userId;
    private File p12ClientCert;

    @Before
    public void cleanup() {
        deleteIfExist("crt");
        deleteIfExist("key");
        deleteIfExist("jks");
        deleteIfExist("p12");

    }

    public String createAttribute() {
        RestTemplate adminRestTemplate = plainRestTemplate(AA_URL);

        AttributeCreationRequest attributeCreationRequest = new AttributeCreationRequest();
        attributeCreationRequest.setName("role");
        attributeCreationRequest.setType(AttributeType.STRING);
        attributeCreationRequest.setValues(Lists.newArrayList("student", "professor"));

        ResponseEntity<PublicAttributeResponse> exchange = adminRestTemplate
                .exchange("/attributes", HttpMethod.POST, new HttpEntity<>(attributeCreationRequest),
                        PublicAttributeResponse.class);

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.CREATED);
        return exchange.getBody().getId();
    }

    private String createUser(String attributeId) {
        RestTemplate adminRestTemplate = plainRestTemplate(AA_URL);

        CreateUserRequest createUserRequest = new CreateUserRequest(
                email,
                Sets.newHashSet(
                        new AttributeValueRequest(
                                attributeId,
                                Sets.newHashSet("student"))));

        ResponseEntity<UserResponse> exchange = adminRestTemplate
                .exchange("/users", HttpMethod.POST, new HttpEntity<>(createUserRequest), UserResponse.class);

        Java6Assertions.assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.CREATED);
        return exchange.getBody().getId();
    }

    public void createUser() {
        attributeId = createAttribute();
        userId = createUser(attributeId);
    }

    private File certificateRequest() {
        RestTemplate unAuthorizedRestTemplate = sslRestTemplate(CA_URL);
        try {
            PKCS10CertificationRequest pkcs10CertificationRequest = certificateRequestFactory
                    .create(email, clientKeyPair);

            CertificateRequest certificateRequest = new CertificateRequest(
                    KeyConverter.from(pkcs10CertificationRequest.getEncoded()).toBase64()
            );

            ResponseEntity<CaCertificateResponse> exchange = unAuthorizedRestTemplate.exchange(
                    "/certificates",
                    HttpMethod.POST,
                    new HttpEntity(certificateRequest),
                    CaCertificateResponse.class
            );

            assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.CREATED);
            X509Certificate x509Certificate = KeyConverter.from(exchange.getBody().getCertificate())
                    .toX509Certificate();

            Path key = toFile("key", certificateUtils.pemFormat(clientKeyPair.getPrivate()));
            Path cert = toFile("crt", certificateUtils.pemFormat(x509Certificate));

            return generateP12KeyStore(key, cert);

        } catch (OperatorCreationException | IOException e) {
            throw new RuntimeException(e);
        }


    }

    private File generateP12KeyStore(Path key, Path cert) {
        cmdExec("openssl pkcs12 -export -clcerts -in " + cert.toString() + " -inkey " + key.toString() +" -out ./" + email + ".p12 -password pass:foobar");
        cmdExec("keytool -importkeystore -destkeystore " + email + ".jks -srckeystore ./" + email + ".p12 -srcstorepass foobar -srcstoretype PKCS12 -storepass foobar -keypass foobar");
        return Paths.get("./" + email + ".jks").toFile();
    }

    private void cmdExec(String cmd) {
        Process p;
        try {
            p = Runtime.getRuntime().exec(cmd);
            int term = p.waitFor();

            if(term != 0) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                String line;
                while ((line = reader.readLine())!= null) {
                    System.err.println(line);
                }

                throw new RuntimeException("Exit code was " + term);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            while ((line = reader.readLine())!= null) {
                System.out.println(line);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Path toFile(String suffix, String pemFormat) {
        try {
            Path path = deleteIfExist(suffix);
            Path tempFile = Files.createFile(path);
            Files.write(tempFile, pemFormat.getBytes());
            return tempFile;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path deleteIfExist(String suffix) {
        Path path = Paths.get("./" + email + "." + suffix);
        if (path.toFile().exists()) {
            path.toFile().delete();
        }
        return path;
    }

    public void registerDevice() {
        createUser();

        p12ClientCert = certificateRequest();
    }

    private void getUser() {
        RestTemplate restTemplate = plainRestTemplate(AA_URL);

        ResponseEntity<UserResponse> exchange = restTemplate.exchange(
                "/users/" + email,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                UserResponse.class
        );

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(exchange.getBody().getDevices().isEmpty());
        assertThat(exchange.getBody().getUnapprovedDevices()).hasSize(1);
    }

    private void approveDevice() {
        RestTemplate restTemplate = plainRestTemplate(AA_URL);

        ResponseEntity<UserResponse> exchange = restTemplate.exchange(
                "/users/" + email,
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                UserResponse.class
        );

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(exchange.getBody().getUnapprovedDevices().isEmpty());
        assertThat(exchange.getBody().getDevices()).hasSize(1);
    }

    @Test
    public void adminApproval() {
        registerDevice();
        getUser();
        approveDevice();
    }



}
