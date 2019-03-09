package de.tuberlin.tfdacmacs.workflows;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.tuberlin.tfdacmacs.IntegrationTestSuite;
import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;
import de.tuberlin.tfdacmacs.dto.attributeauthority.attribute.AttributeCreationRequest;
import de.tuberlin.tfdacmacs.dto.attributeauthority.attribute.AttributeType;
import de.tuberlin.tfdacmacs.dto.attributeauthority.attribute.PublicAttributeResponse;
import de.tuberlin.tfdacmacs.dto.attributeauthority.user.AttributeValueRequest;
import de.tuberlin.tfdacmacs.dto.attributeauthority.user.CreateUserRequest;
import de.tuberlin.tfdacmacs.dto.attributeauthority.user.UserResponse;
import de.tuberlin.tfdacmacs.dto.centralauthority.certificate.CaCertificateResponse;
import de.tuberlin.tfdacmacs.dto.centralauthority.certificate.CertificateRequest;
import de.tuberlin.tfdacmacs.dto.centralauthority.gpp.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.dto.centralauthority.user.DeviceResponse;
import de.tuberlin.tfdacmacs.dto.centralauthority.user.EncryptedAttributeValueKeyDTO;
import it.unisa.dia.gas.jpbc.Element;
import org.assertj.core.api.Java6Assertions;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.util.encoders.Base64;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.cert.X509Certificate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class RegisterUser extends IntegrationTestSuite {

    private String attributeId;
    private File clientKeyStore;
    private String deviceId;
    private GlobalPublicParameter gpp;
    private Element attributeSecretKey;

    @Before
    public void cleanup() {
        deleteIfExist("crt");
        deleteIfExist("key");
        deleteIfExist("jks");
        deleteIfExist("p12");

    }

    public String createAttribute() {
        RestTemplate adminRestTemplate = sslRestTemplate(AA_URL);

        AttributeCreationRequest attributeCreationRequest = new AttributeCreationRequest();
        attributeCreationRequest.setName("role");
        attributeCreationRequest.setType(AttributeType.STRING);
        attributeCreationRequest.setValues(Lists.newArrayList("student", "professor"));

        ResponseEntity<PublicAttributeResponse> exchange = adminRestTemplate
                .exchange("/attributes", HttpMethod.POST, new HttpEntity<>(attributeCreationRequest, basicAuth()),
                        PublicAttributeResponse.class);

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.CREATED);
        return exchange.getBody().getId();
    }

    private String createUser(String attributeId) {
        RestTemplate adminRestTemplate = sslRestTemplate(AA_URL);

        CreateUserRequest createUserRequest = new CreateUserRequest(
                email,
                Sets.newHashSet(
                        new AttributeValueRequest(
                                attributeId,
                                Sets.newHashSet("student"))));

        ResponseEntity<UserResponse> exchange = adminRestTemplate
                .exchange("/users", HttpMethod.POST, new HttpEntity<>(createUserRequest, basicAuth()), UserResponse.class);

        Java6Assertions.assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.CREATED);
        return exchange.getBody().getId();
    }

    public void createUser() {
        attributeId = createAttribute();
        createUser(attributeId);
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

        clientKeyStore = certificateRequest();
    }

    private String getUser() {
        RestTemplate restTemplate = sslRestTemplate(AA_URL);

        ResponseEntity<UserResponse> exchange = restTemplate.exchange(
                "/users/" + email,
                HttpMethod.GET,
                new HttpEntity<>(basicAuth()),
                UserResponse.class
        );

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(exchange.getBody().getDevices().isEmpty());
        assertThat(exchange.getBody().getUnapprovedDevices()).hasSize(1);
        return exchange.getBody().getUnapprovedDevices().get(0).getId();
    }

    private void approveDevice() {
        RestTemplate restTemplate = sslRestTemplate(AA_URL);

        ResponseEntity<UserResponse> exchange = restTemplate.exchange(
                "/users/" + email + "/approve/" + deviceId,
                HttpMethod.PUT,
                new HttpEntity<>(basicAuth()),
                UserResponse.class
        );

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(exchange.getBody().getUnapprovedDevices().isEmpty());
        assertThat(exchange.getBody().getDevices()).hasSize(1);
    }

    public void adminApproval() {
        registerDevice();
        deviceId = getUser();
        approveDevice();
    }

    private void getGPP() {
        RestTemplate restTemplate = mutalAuthenticationRestTemplate(CA_URL, clientKeyStore.toPath().toString());

        ResponseEntity<GlobalPublicParameterDTO> exchange = restTemplate.exchange(
                "/gpp",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                GlobalPublicParameterDTO.class
        );

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        gpp = exchange.getBody().toGlobalPublicParameter(pairingGenerator);
    }

    private void getRawAttributeKeys() {
        RestTemplate restTemplate = sslRestTemplate(AA_URL);

        ResponseEntity<UserResponse> exchange = restTemplate.exchange(
                "/users/" + email,
                HttpMethod.GET,
                new HttpEntity<>(basicAuth()),
                UserResponse.class
        );

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        attributeSecretKey = ElementConverter
                .convert(extractFromSet(exchange.getBody().getAttributes()).getKey(), gpp.g1());
    }

    private void getAttributeKeys() throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException {
        RestTemplate restTemplate = mutalAuthenticationRestTemplate(CA_URL, clientKeyStore.toPath().toString());

        ResponseEntity<DeviceResponse> exchange = restTemplate.exchange(
                String.format("/users/%s/devices/%s", email, deviceId),
                HttpMethod.GET,
                HttpEntity.EMPTY,
                DeviceResponse.class
        );

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);

        DeviceResponse body = exchange.getBody();
        EncryptedAttributeValueKeyDTO encryptedAttributeValueKeyDTO = extractFromSet(body.getEncryptedAttributeValueKeys());

        String encryptedKey = body.getEncryptedKey();
        Key key = symmetricCryptEngine.createKeyFromBytes(asymmetricCryptEngine.decryptRaw(encryptedKey, clientKeyPair.getPrivate()));
        byte[] rawElement = symmetricCryptEngine.decryptRaw(Base64.decode(encryptedAttributeValueKeyDTO.getEncryptedKey()), key);
        byte[] originalBytes = attributeSecretKey.toBytes();

        assertSameElements(rawElement, originalBytes);
    }


    private <T> T extractFromSet(Set<T> set) {
        return set.stream().findFirst().get();
    }

    @Test
    public void retrieveAttributeKeys() throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException {
        adminApproval();
        getGPP();

        getRawAttributeKeys();
        getAttributeKeys();
    }

}
