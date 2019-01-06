package de.tuberlin.tfdacmacs.centralserver.user;

import com.google.common.collect.Sets;
import de.tuberlin.tfdacmacs.RestTestSuite;
import de.tuberlin.tfdacmacs.centralserver.authority.data.AttributeAuthority;
import de.tuberlin.tfdacmacs.centralserver.user.data.Device;
import de.tuberlin.tfdacmacs.centralserver.user.data.EncryptedAttributeValueKey;
import de.tuberlin.tfdacmacs.centralserver.user.data.User;
import de.tuberlin.tfdacmacs.lib.user.data.DeviceState;
import de.tuberlin.tfdacmacs.lib.user.data.dto.DeviceResponse;
import de.tuberlin.tfdacmacs.lib.user.data.dto.EncryptedAttributeValueKeyDTO;
import de.tuberlin.tfdacmacs.lib.user.data.dto.UserCreationRequest;
import de.tuberlin.tfdacmacs.lib.user.data.dto.UserResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class UserRestTest extends RestTestSuite {

    @Before
    public void setup() {
        AttributeAuthority attributeAuthority = new AttributeAuthority(aid, UUID.randomUUID().toString());
        attributeAuthorityDB.insert(attributeAuthority);
    }

    @Test
    public void createUser() {
        mutalAuthenticationRestTemplate(AUTHORITY_KEYSTORE);

        UserCreationRequest creationRequest = new UserCreationRequest(email);

        ResponseEntity<UserResponse> userCreationResponseResponseEntity = sslRestTemplate
                .exchange("/users", HttpMethod.POST, new HttpEntity(creationRequest), UserResponse.class);
        assertThat(userCreationResponseResponseEntity.getStatusCode()).isEqualByComparingTo(HttpStatus.CREATED);
        UserResponse body = userCreationResponseResponseEntity.getBody();
        String id = body.getId();
        assertThat(id).isNotBlank().isEqualTo(email);
        assertThat(body.getAuthorityId()).isEqualTo(aid);
        assertThat(userDB.exist(email)).isTrue();
        assertThat(body.getDevices()).isEmpty();
    }

    @Test
    public void findUser() {
        mutalAuthenticationRestTemplate(AUTHORITY_KEYSTORE);

        String userId = UUID.randomUUID().toString();
        User user = new User(userId, aid);
        userDB.insert(user);

        ResponseEntity<UserResponse> response = sslRestTemplate
                .exchange("/users/" + userId, HttpMethod.GET, HttpEntity.EMPTY, UserResponse.class);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        UserResponse body = response.getBody();
        assertThat(body.getId()).isEqualTo(userId);
    }

    @Test
    public void findUsers() {
        mutalAuthenticationRestTemplate(AUTHORITY_KEYSTORE);

        String user1Id = UUID.randomUUID().toString();
        String user2Id = UUID.randomUUID().toString();
        String aid2 = UUID.randomUUID().toString();

        User user1 = new User(user1Id, aid);
        User user2 = new User(user2Id, aid2);

        userDB.insert(user1);
        userDB.insert(user2);

        ResponseEntity<List<UserResponse>> response = sslRestTemplate
                .exchange("/users", HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<List<UserResponse>>(){});

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        List<UserResponse> body = response.getBody();
        assertThat(body).hasSize(1);
        assertThat(body.get(0).getId()).isEqualTo(user1Id);
    }

    @Test
    public void getDevice_returnError_whileAdminApprovalNeeded() {
        String certId = UUID.randomUUID().toString();
        User user = new User(email, aid);
        Device device = new Device(certId, null, Sets.newHashSet(), DeviceState.WAITING_FOR_APPROVAL);
        user.setDevices(Sets.newHashSet(device));
        userDB.insert(user);

        ResponseEntity<DeviceResponse> response = sslRestTemplate
                .exchange(String.format("/users/%s/devices/%s", email, device.getCertificateId()), HttpMethod.GET, HttpEntity.EMPTY, DeviceResponse.class);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.PRECONDITION_FAILED);
    }

    @Test
    public void getDevice_passes() {
        String certId = UUID.randomUUID().toString();
        String encryptedKey = "someKey";
        String attributeValueId = "aa.tu-berlin.de:role:student";
        String attributeEncryptedValue = "somevalue";
        User user = new User(email, aid);
        Device device = new Device(certId,encryptedKey , Sets.newHashSet(new EncryptedAttributeValueKey(attributeValueId, attributeEncryptedValue)), DeviceState.ACTIVE);
        user.setDevices(Sets.newHashSet(device));
        userDB.insert(user);

        ResponseEntity<DeviceResponse> response = sslRestTemplate
                .exchange(String.format("/users/%s/devices/%s", email, device.getCertificateId()), HttpMethod.GET, HttpEntity.EMPTY, DeviceResponse.class);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        DeviceResponse body = response.getBody();
        assertThat(body.getCertificateId()).isEqualTo(certId);
        assertThat(body.getDeviceState()).isEqualByComparingTo(DeviceState.ACTIVE);
        assertThat(body.getEncryptedKey()).isEqualTo(encryptedKey);
        assertThat(body.getEncryptedAttributeValueKeys()).hasSize(1);
        EncryptedAttributeValueKeyDTO encryptedAttributeValueKeyDTO = body.getEncryptedAttributeValueKeys().stream()
                .findFirst().get();
        assertThat(encryptedAttributeValueKeyDTO.getAttributeValueId()).isEqualTo(attributeValueId);
        assertThat(encryptedAttributeValueKeyDTO.getEncryptedKey()).isEqualTo(attributeEncryptedValue);
    }
}
