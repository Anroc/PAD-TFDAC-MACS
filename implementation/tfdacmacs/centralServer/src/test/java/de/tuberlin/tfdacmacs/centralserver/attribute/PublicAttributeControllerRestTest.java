package de.tuberlin.tfdacmacs.centralserver.attribute;

import com.google.common.collect.Sets;
import de.tuberlin.tfdacmacs.RestTestSuite;
import de.tuberlin.tfdacmacs.centralserver.authority.data.AttributeAuthority;
import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import de.tuberlin.tfdacmacs.lib.attributes.data.AbstractAttribute;
import de.tuberlin.tfdacmacs.lib.attributes.data.AttributeType;
import de.tuberlin.tfdacmacs.lib.attributes.data.PublicAttribute;
import de.tuberlin.tfdacmacs.lib.attributes.data.PublicAttributeValue;
import de.tuberlin.tfdacmacs.lib.attributes.data.dto.AttributeCreationRequest;
import de.tuberlin.tfdacmacs.lib.attributes.data.dto.AttributeValueCreationRequest;
import de.tuberlin.tfdacmacs.lib.attributes.data.dto.PublicAttributeResponse;
import de.tuberlin.tfdacmacs.lib.attributes.data.dto.PublicAttributeValueResponse;
import it.unisa.dia.gas.jpbc.Element;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PublicAttributeControllerRestTest extends RestTestSuite {

    private PublicAttribute publicAttribute;
    private PublicAttributeValue publicAttributeValue;

    private String serializedPublicKey;

    @Before
    public void setup() {
        this.publicAttributeValue = new PublicAttributeValue(
                gppProvider.getGlobalPublicParameter().getPairing().getG1().newRandomElement(),
                "test"
        );
        this.publicAttribute = AbstractAttribute.createPublicAttribute(
                aid,
                "this-is-a",
                Sets.newHashSet(this.publicAttributeValue),
                AttributeType.STRING
        );

        this.serializedPublicKey = ElementConverter.convert(publicAttributeValue.getKey());

        attributeAuthorityDB.insert(new AttributeAuthority(aid, "someCertId"));
    }

    @Test
    public void createAttribute() {
        mutalAuthenticationRestTemplate(RestTestSuite.AUTHORITY_KEYSTORE);

        AttributeCreationRequest attributeCreationRequest = AttributeCreationRequest.from(publicAttribute);

        ResponseEntity<PublicAttributeResponse> exchange = sslRestTemplate.exchange(
                "/attributes",
                HttpMethod.POST,
                new HttpEntity<>(attributeCreationRequest),
                PublicAttributeResponse.class
        );

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.CREATED);

        PublicAttributeResponse body = exchange.getBody();
        assertPublicAttributeResponse(body);
    }

    @Test
    public void getAttributes() {
        publicAttributeDB.insert(publicAttribute);

        ResponseEntity<List<PublicAttributeResponse>> exchange =
                sslRestTemplate.exchange(
                        "/attributes",
                        HttpMethod.GET,
                        HttpEntity.EMPTY,
                        new ParameterizedTypeReference<List<PublicAttributeResponse>>() {}
                );

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);

        List<PublicAttributeResponse> list = exchange.getBody();
        assertThat(list).hasSize(1);
        PublicAttributeResponse body = list.get(0);
        assertPublicAttributeResponse(body);
    }

    @Test
    public void getAttribute() {
        publicAttributeDB.insert(publicAttribute);

        ResponseEntity<PublicAttributeResponse> exchange =
                sslRestTemplate.exchange(
                        "/attributes/" + publicAttribute.getId(),
                        HttpMethod.GET,
                        HttpEntity.EMPTY,
                        PublicAttributeResponse.class
                );

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);

        PublicAttributeResponse body = exchange.getBody();
        assertPublicAttributeResponse(body);
    }

    @Test
    public void addAttributeValue() {
        mutalAuthenticationRestTemplate(RestTestSuite.AUTHORITY_KEYSTORE);


        publicAttributeDB.insert(publicAttribute);

        Element originalPublicKey = gppProvider.getGlobalPublicParameter().getPairing().getG1().newRandomElement();
        String value = "otherTest";

        AttributeValueCreationRequest attributeValueCreationRequest = new AttributeValueCreationRequest(
            value, ElementConverter.convert(originalPublicKey)
        );
        ResponseEntity<PublicAttributeValueResponse> exchange =
                sslRestTemplate.exchange(
                        "/attributes/" + publicAttribute.getId(),
                        HttpMethod.POST,
                        new HttpEntity<>(attributeValueCreationRequest),
                        PublicAttributeValueResponse.class
                );

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.CREATED);

        PublicAttributeValueResponse body = exchange.getBody();
        assertThat(body.getValue()).isEqualTo(value);
        assertThat(body.getPublicKey()).isEqualTo(ElementConverter.convert(originalPublicKey));
    }

    @Test
    public void getAttributeValue() {
        publicAttributeDB.insert(publicAttribute);

        ResponseEntity<PublicAttributeValueResponse> exchange =
                sslRestTemplate.exchange(
                        "/attributes/" + publicAttribute.getId() + "/values/" + publicAttributeValue.getValue(),
                        HttpMethod.GET,
                        HttpEntity.EMPTY,
                        PublicAttributeValueResponse.class
                );

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);

        PublicAttributeValueResponse body = exchange.getBody();
        assertThat(body.getValue()).isEqualTo(publicAttributeValue.getValue());
        assertThat(body.getPublicKey()).isEqualTo(serializedPublicKey);
    }

    public void assertPublicAttributeResponse(PublicAttributeResponse body) {
        assertThat(body.getId()).isEqualTo(publicAttribute.getId());
        assertThat(body.getAuthorityDomain()).isEqualTo(publicAttribute.getAuthorityDomain());
        assertThat(body.getType()).isEqualByComparingTo(publicAttribute.getType());
        assertThat(RestTestSuite.findFirst(body.getValues()).getValue()).isEqualTo(publicAttributeValue.getValue());
        assertThat(RestTestSuite.findFirst(body.getValues()).getPublicKey()).isEqualTo(serializedPublicKey);
    }
}
