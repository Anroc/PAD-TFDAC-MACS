package de.tuberlin.tfdacmacs.user;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.tuberlin.tfdacmacs.IntegrationTestSuite;
import de.tuberlin.tfdacmacs.attributeauthority.attribute.data.dto.AttributeCreationRequest;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.dto.AttributeValueRequest;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.dto.AttributeValueResponse;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.dto.CreateUserRequest;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.dto.UserResponse;
import de.tuberlin.tfdacmacs.lib.attributes.data.AttributeType;
import de.tuberlin.tfdacmacs.lib.attributes.data.dto.PublicAttributeResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class UserIntegrationTest extends IntegrationTestSuite {

    private String attributeId;
    private String email;

    @Before
    public void setup() {
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
    public void getAttributeKeys() throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException {
        // setup
        String sign = gppTestFactory.getCryptEngine().sign(email);
        createUser_passes_nonNonExistingAttributeValue();

        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, sign);
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
    }
}
