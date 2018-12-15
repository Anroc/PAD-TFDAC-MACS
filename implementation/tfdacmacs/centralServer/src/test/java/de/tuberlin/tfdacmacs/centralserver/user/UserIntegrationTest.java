package de.tuberlin.tfdacmacs.centralserver.user;

import de.tuberlin.tfdacmacs.IntegrationTestSuite;
import de.tuberlin.tfdacmacs.centralserver.user.data.dto.UserCreationRequest;
import de.tuberlin.tfdacmacs.centralserver.user.data.dto.UserCreationResponse;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class UserIntegrationTest extends IntegrationTestSuite {

    @Test
    public void createUser() {
        UserCreationRequest userCreationRequest = new UserCreationRequest("test@tu-berlin.de");

        ResponseEntity<UserCreationResponse> userCreationResponseResponseEntity = restTemplate
                .exchange("/users", HttpMethod.POST, new HttpEntity(userCreationRequest), UserCreationResponse.class);
        assertThat(userCreationResponseResponseEntity.getStatusCode()).isEqualByComparingTo(HttpStatus.CREATED);
        UserCreationResponse body = userCreationResponseResponseEntity.getBody();
        String id = body.getId();
        assertThat(id).isNotBlank().isEqualTo("test@tu-berlin.de");
        assertThat(cryptEngine.isSignatureAuthentic(body.getIdSignature(), id, cryptEngine.getPublicKey())).isTrue();
        assertThat(userDB.exist(id)).isTrue();
    }
}
