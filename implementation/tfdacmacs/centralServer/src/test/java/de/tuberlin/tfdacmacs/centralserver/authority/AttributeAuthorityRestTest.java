package de.tuberlin.tfdacmacs.centralserver.authority;

import de.tuberlin.tfdacmacs.RestTestSuite;
import de.tuberlin.tfdacmacs.centralserver.authority.data.AttributeAuthority;
import de.tuberlin.tfdacmacs.centralserver.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.crypto.rsa.StringAsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.lib.authority.AttributeAuthorityPublicKeyRequest;
import de.tuberlin.tfdacmacs.lib.authority.AttributeAuthorityResponse;
import de.tuberlin.tfdacmacs.lib.certificate.data.dto.CertificateRequest;
import it.unisa.dia.gas.jpbc.Element;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.security.KeyPair;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class AttributeAuthorityRestTest extends RestTestSuite {

    private String aid = "aa.tu-berlin.de";
    private KeyPair keyPair = new StringAsymmetricCryptEngine().generateKeyPair();

    @Test
    public void createAuthority() throws IOException, OperatorCreationException {
        CertificateRequest certificateRequest = certificateRequestTestFactory.create(aid, keyPair);

        ResponseEntity<AttributeAuthorityResponse> exchange = restTemplate
                .exchange("/authorities", HttpMethod.POST, new HttpEntity(certificateRequest, basicAuth()),
                        AttributeAuthorityResponse.class);

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.CREATED);
        AttributeAuthorityResponse body = exchange.getBody();
        assertThat(body).hasNoNullFieldsOrPropertiesExcept("publicKey", "signature");
        assertThat(body.getId()).isEqualTo(aid);
        Optional<Certificate> certificateOptional = certificateDB.findEntity(body.getCertificateId());
        assertThat(certificateOptional).isPresent();

        System.out.println(certificateUtils.pemFormat(keyPair.getPrivate()));
        System.out.println(certificateUtils.pemFormat(certificateOptional.get().getCertificate()));
    }

    @Test
    public void getAuthority() {
        AttributeAuthority attributeAuthority = new AttributeAuthority(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );

        attributeAuthorityDB.insert(attributeAuthority);

        ResponseEntity<AttributeAuthorityResponse> exchange = sslRestTemplate
                .exchange("/authorities/" + attributeAuthority.getId(), HttpMethod.GET, HttpEntity.EMPTY,
                        AttributeAuthorityResponse.class);

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        AttributeAuthorityResponse body = exchange.getBody();
        assertThat(body.getId()).isEqualTo(attributeAuthority.getId());
        assertThat(body.getCertificateId()).isEqualTo(attributeAuthority.getCertificateId());
        assertThat(body.getPublicKey()).isNull();
    }

    @Test
    public void putPublicKey() {
        GlobalPublicParameter globalPublicParameter = globalPublicParameterService.getGlobalPublicParameter();
        mutalAuthenticationRestTemplate(AUTHORITY_KEYSTORE);

        AttributeAuthority attributeAuthority = new AttributeAuthority(aid, UUID.randomUUID().toString());
        attributeAuthorityDB.insert(attributeAuthority);

        Element element = globalPublicParameter.getPairing().getG1().newRandomElement();
        String publicKey = ElementConverter.convert(element);
        String signature = "someSignature";
        AttributeAuthorityPublicKeyRequest attributeAuthorityPublicKeyRequest = new AttributeAuthorityPublicKeyRequest(
                publicKey,
                signature
        );

        ResponseEntity<AttributeAuthorityResponse> exchange = sslRestTemplate
                .exchange("/authorities/" + attributeAuthority.getId() + "/public-key",
                        HttpMethod.PUT,
                        new HttpEntity<>(attributeAuthorityPublicKeyRequest),
                        AttributeAuthorityResponse.class);

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        AttributeAuthorityResponse body = exchange.getBody();
        assertThat(body.getId()).isEqualTo(attributeAuthority.getId());
        assertThat(body.getCertificateId()).isEqualTo(attributeAuthority.getCertificateId());
        assertThat(body.getSignature()).isEqualTo(signature);
        assertThat(body.getPublicKey()).isEqualTo(publicKey);
        Element elementPublicKey = attributeAuthorityDB.findEntity(body.getId()).get().getPublicKey().getKey();
        assertThat(elementPublicKey).isEqualTo(element);
    }
}
