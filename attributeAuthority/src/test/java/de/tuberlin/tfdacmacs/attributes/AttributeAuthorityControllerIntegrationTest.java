package de.tuberlin.tfdacmacs.attributes;

import com.google.common.collect.Lists;
import de.tuberlin.tfdacmacs.IntegrationTestSuite;
import de.tuberlin.tfdacmacs.attributeauthority.attribute.data.dto.AttributeCreationRequest;
import de.tuberlin.tfdacmacs.lib.attributes.data.Attribute;
import de.tuberlin.tfdacmacs.lib.attributes.data.AttributeType;
import de.tuberlin.tfdacmacs.lib.attributes.data.dto.PublicAttributeResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AttributeAuthorityControllerIntegrationTest extends IntegrationTestSuite {

    private Attribute attribute;

    @Before
    public void setup() {
        attribute = attributeTestFactory.create();
        attributeDB.insert(attribute);
    }

    @Test
    public void createAttribute() {
        AttributeCreationRequest attributeCreationRequest = new AttributeCreationRequest();
        attributeCreationRequest.setName("role");
        attributeCreationRequest.setType(AttributeType.STRING);
        attributeCreationRequest.setValues(Lists.newArrayList("student", "professor"));

        ResponseEntity<PublicAttributeResponse> exchange = restTemplate
                .exchange("/attributes", HttpMethod.POST, new HttpEntity<>(attributeCreationRequest),
                        PublicAttributeResponse.class);

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.CREATED);

        PublicAttributeResponse body = exchange.getBody();
        assertThat(body.getName()).isEqualTo("role");
        assertThat(body.getId()).isEqualTo("aa.tu-berlin.de.role");
        assertThat(body.getType()).isEqualByComparingTo(AttributeType.STRING);
        assertThat(body.getValues()).hasSize(2);
        assertThat(body.getValues().get(0).getPublicKey()).isNotNull().isNotBlank();
        assertThat(body.getValues().get(0).getValue()).isNotNull().isInstanceOf(String.class);
        assertThat(body.getValues().get(1).getPublicKey()).isNotNull().isNotBlank();
        assertThat(body.getValues().get(1).getValue()).isNotNull().isInstanceOf(String.class);
        assertThat(body.getValues().get(0).getPublicKey()).isNotEqualTo(body.getValues().get(1).getPublicKey());

        assertThat(attributeDB.exist(body.getId())).isTrue();
    }

    @Test
    public void createAttribute_fails_forInvalidName() {
        AttributeCreationRequest attributeCreationRequest = new AttributeCreationRequest();

        attributeCreationRequest.setName("asd.a");
        attributeCreationRequest.setType(AttributeType.STRING);
        attributeCreationRequest.setValues(Lists.newArrayList("student", "professor"));

        ResponseEntity<PublicAttributeResponse> exchange = restTemplate
                .exchange("/attributes", HttpMethod.POST, new HttpEntity<>(attributeCreationRequest),
                        PublicAttributeResponse.class);

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void createAttribute_fails_forInvalidValue() {
        AttributeCreationRequest attributeCreationRequest = new AttributeCreationRequest();

        attributeCreationRequest.setName("asd");
        attributeCreationRequest.setType(AttributeType.STRING);
        attributeCreationRequest.setValues(Lists.newArrayList("student:asd", "professor:asd"));

        ResponseEntity<PublicAttributeResponse> exchange = restTemplate
                .exchange("/attributes", HttpMethod.POST, new HttpEntity<>(attributeCreationRequest),
                        PublicAttributeResponse.class);

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getAllAttributes() {
        ResponseEntity<List<PublicAttributeResponse>> responseEntity = restTemplate.exchange("/attributes",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<PublicAttributeResponse>>() {});

        assertThat(responseEntity.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).hasSize(1);
    }

    @Test
    public void getAttribute() {
        ResponseEntity<PublicAttributeResponse> responseEntity = restTemplate.exchange(
                String.format("/attributes/%s", attribute.getId()), HttpMethod.GET, HttpEntity.EMPTY,
                PublicAttributeResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
    }



}
