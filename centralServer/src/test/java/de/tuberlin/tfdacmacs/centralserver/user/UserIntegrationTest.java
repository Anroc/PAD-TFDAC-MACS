package de.tuberlin.tfdacmacs.centralserver.user;

import de.tuberlin.tfdacmacs.IntegrationTestSuite;
import de.tuberlin.tfdacmacs.lib.user.data.dto.UserCreationRequest;
import de.tuberlin.tfdacmacs.lib.user.data.dto.UserResponse;
import de.tuberlin.tfdacmacs.centralserver.authority.data.AttributeAuthority;
import de.tuberlin.tfdacmacs.centralserver.user.data.User;
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

public class UserIntegrationTest extends IntegrationTestSuite {

    private String email = "test@tu-berlin.de";
    private String aid = "aa.tu-berlin.de";

    @Before
    public void setup() {
        mutalAuthenticationRestTemplate(AUTHORITY_KEYSTORE);

        AttributeAuthority attributeAuthority = new AttributeAuthority(aid, UUID.randomUUID().toString());
        attributeAuthorityDB.insert(attributeAuthority);
    }

    @Test
    public void createUser() {
        UserCreationRequest creationRequest = new UserCreationRequest(email, aid);

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
    public void findUsers() {
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
}
