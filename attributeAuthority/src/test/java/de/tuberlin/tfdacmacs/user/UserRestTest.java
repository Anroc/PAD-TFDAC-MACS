package de.tuberlin.tfdacmacs.user;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.tuberlin.tfdacmacs.RestTestSuite;
import de.tuberlin.tfdacmacs.attributeauthority.attribute.data.dto.AttributeCreationRequest;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.User;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.dto.AttributeValueRequest;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.dto.AttributeValueResponse;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.dto.CreateUserRequest;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.dto.UserResponse;
import de.tuberlin.tfdacmacs.crypto.rsa.StringAsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.StringSymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;
import de.tuberlin.tfdacmacs.lib.attributes.data.AttributeType;
import de.tuberlin.tfdacmacs.lib.attributes.data.dto.PublicAttributeResponse;
import de.tuberlin.tfdacmacs.lib.certificate.data.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.lib.user.data.DeviceState;
import de.tuberlin.tfdacmacs.lib.user.data.dto.DeviceResponse;
import de.tuberlin.tfdacmacs.lib.user.data.dto.DeviceUpdateRequest;
import de.tuberlin.tfdacmacs.lib.user.data.dto.EncryptedAttributeValueKeyDTO;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Set;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

public class UserRestTest extends RestTestSuite {

    private String attributeId;
    private String email;
    private final StringAsymmetricCryptEngine asymCryptEngine = new StringAsymmetricCryptEngine();
    private final StringSymmetricCryptEngine symCryptEngine = new StringSymmetricCryptEngine();
    private final KeyPair userKeyPair = asymCryptEngine.generateKeyPair();
    private CertificateResponse userCertificateResponse;

    @Before
    public void setup()
            throws OperatorCreationException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException,
            IOException {
        AttributeCreationRequest attributeCreationRequest = new AttributeCreationRequest();

        attributeCreationRequest.setName("role");
        attributeCreationRequest.setType(AttributeType.STRING);
        attributeCreationRequest.setValues(Lists.newArrayList("student"));
        ResponseEntity<PublicAttributeResponse> exchange = restTemplate
                .exchange("/attributes", HttpMethod.POST, new HttpEntity<>(attributeCreationRequest),
                        PublicAttributeResponse.class);

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.CREATED);
        this.attributeId = exchange.getBody().getId();
        this.email = "test@tu-berlin.de";

        X509Certificate x509Certificate = certificateTestFactory.createEntityCertificate(
                caKeyPair, rootCertificate, email, userKeyPair
        );

        userCertificateResponse = new CertificateResponse(
                certificateUtils.fingerprint(x509Certificate),
                KeyConverter.from(x509Certificate).toBase64());
        doReturn(userCertificateResponse).when(caClient).getCertificate(userCertificateResponse.getId());

        de.tuberlin.tfdacmacs.lib.user.data.dto.UserResponse userResponse = new de.tuberlin.tfdacmacs.lib.user.data.dto.UserResponse(
                email,
                attributeAuthorityConfig.getId(),
                Sets.newHashSet(
                        new DeviceResponse(userCertificateResponse.getId(), DeviceState.WAITING_FOR_APPROVAL, null, Sets.newHashSet())
                )
        );
        doReturn(userResponse).when(caClient).getUser(email);

        doReturn(
                new DeviceResponse(
                        userCertificateResponse.getId(),
                        DeviceState.ACTIVE,
                        "somekey",
                        Sets.newHashSet()
                )
        ).when(caClient).updateDevice(
                eq(email),
                eq(userCertificateResponse.getId()),
                any(DeviceUpdateRequest.class)
        );
    }


    @Test
    public void createUser_passes_onExistingAttributeValue() {
        CreateUserRequest createUserRequest = new CreateUserRequest(
                email,
                Sets.newHashSet(
                        new AttributeValueRequest(
                                attributeId,
                                Sets.newHashSet("student"))));

        ResponseEntity<UserResponse> exchange = restTemplate
                .exchange("/users", HttpMethod.POST, new HttpEntity<>(createUserRequest), UserResponse.class);

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.CREATED);
        UserResponse body = exchange.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getId()).isEqualTo(email);
        assertThat(body.getAttributes()).hasSize(1);
        AttributeValueResponse attributeValueResponse = body.getAttributes().stream().findFirst().get();
        assertThat(attributeValueResponse).isNotNull();
        assertThat(attributeValueResponse.getAttributeId()).isEqualTo(attributeId);
        assertThat(attributeValueResponse.getValue()).isInstanceOf(String.class).isEqualTo("student");
        assertThat(attributeValueResponse.getKey()).isNotBlank();
    }

    @Test
    public void createUser_passes_nonNonExistingAttributeValue() {
        CreateUserRequest createUserRequest = new CreateUserRequest(
                email,
                Sets.newHashSet(
                        new AttributeValueRequest(
                                attributeId,
                                Sets.newHashSet("professor"))));

        ResponseEntity<UserResponse> exchange = restTemplate
                .exchange("/users", HttpMethod.POST, new HttpEntity<>(createUserRequest), UserResponse.class);

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.CREATED);
        UserResponse body = exchange.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getId()).isEqualTo(email);
        assertThat(body.getAttributes()).hasSize(1);
        AttributeValueResponse attributeValueResponse = body.getAttributes().stream().findFirst().get();
        assertThat(attributeValueResponse).isNotNull();
        assertThat(attributeValueResponse.getAttributeId()).isEqualTo(attributeId);
        assertThat(attributeValueResponse.getValue()).isInstanceOf(String.class).isEqualTo("professor");
        assertThat(attributeValueResponse.getKey()).isNotBlank();
    }

    @Test
    public void getAttributeKeys() {
        // setup
        createUser_passes_nonNonExistingAttributeValue();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<UserResponse> exchange = restTemplate
                .exchange("/users/" + email, HttpMethod.GET, httpEntity, UserResponse.class);

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        UserResponse body = exchange.getBody();
        assertThat(body.getId()).isEqualTo(email);
        assertThat(body.getAttributes()).hasSize(1);
        AttributeValueResponse attributeValueResponse = body.getAttributes().stream().findFirst().get();
        assertThat(attributeValueResponse).isNotNull();
        assertThat(attributeValueResponse.getAttributeId()).isEqualTo(attributeId);
        assertThat(attributeValueResponse.getValue()).isInstanceOf(String.class).isEqualTo("professor");
        assertThat(attributeValueResponse.getKey()).isNotBlank();

        assertThat(body.getDevices()).isEmpty();
        assertThat(body.getUnapprovedDevices()).hasSize(1);
        assertThat(body.getUnapprovedDevices().get(0).getId()).isEqualTo(userCertificateResponse.getId());
        System.out.println(body.getUnapprovedDevices().get(0).getId());
        assertThat(body.getUnapprovedDevices().get(0).getCertificate()).isEqualTo(userCertificateResponse.getCertificate());
    }

    @Test
    public void approveUser() throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException {
        getAttributeKeys();

        ResponseEntity<UserResponse> exchange = restTemplate.exchange(
                String.format("/users/%s/approve/%s", email, userCertificateResponse.getId()),
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                UserResponse.class
        );

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);

        User user = userDB.findEntity(email).get();
        assertThat(user.getDevices()).hasSize(1);
        assertThat(user.getUnapprovedDevices()).isEmpty();

        ArgumentCaptor<DeviceUpdateRequest> argumentCaptor = ArgumentCaptor.forClass(DeviceUpdateRequest.class);
        verify(caClient).updateDevice(
                eq(email),
                eq(userCertificateResponse.getId()),
                argumentCaptor.capture()
        );

        DeviceUpdateRequest deviceUpdateRequest = argumentCaptor.getValue();
        assertThat(deviceUpdateRequest.getDeviceState()).isEqualByComparingTo(DeviceState.ACTIVE);
        assertThat(deviceUpdateRequest.getEncryptedAttributeValueKeys()).hasSize(1);
        EncryptedAttributeValueKeyDTO encryptedAttributeValueKeyDTO = extractFromSet(deviceUpdateRequest
                .getEncryptedAttributeValueKeys());
        assertThat(encryptedAttributeValueKeyDTO.getAttributeValueId()).isEqualTo(extractFromSet(user.getAttributes()).getAttributeValueId());

        String encryptedKey = deviceUpdateRequest.getEncryptedKey();
        Key key = symCryptEngine.createKeyFromBytes(asymCryptEngine.decryptRaw(encryptedKey, userKeyPair.getPrivate()));
        byte[] rawElement = symCryptEngine.decryptRaw(encryptedAttributeValueKeyDTO.getEncryptedKey(), key);
        byte[] originalBytes = extractFromSet(user.getAttributes()).getKey().getSecretKey().toBytes();

        assertSameElements(rawElement, originalBytes);
    }

    private <T> T extractFromSet(Set<T> set) {
        return set.stream().findFirst().get();
    }
}
