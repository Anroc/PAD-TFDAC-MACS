package de.tuberlin.tfdacmacs.centralserver.authority;

import de.tuberlin.tfdacmacs.IntegrationTestSuite;
import de.tuberlin.tfdacmacs.lib.authority.AttributeAuthorityResponse;
import de.tuberlin.tfdacmacs.lib.certificate.data.dto.CertificateRequest;
import de.tuberlin.tfdacmacs.crypto.rsa.StringAsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.centralserver.certificate.data.Certificate;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.security.KeyPair;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class AttributeAuthorityIntegrationTest extends IntegrationTestSuite {

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
        assertThat(body).hasNoNullFieldsOrProperties();
        assertThat(body.getId()).isEqualTo(aid);
        Optional<Certificate> certificateOptional = certificateDB.findEntity(body.getCertificateId());
        assertThat(certificateOptional).isPresent();

        certificateRequestTestFactory.printPEMFormat(keyPair.getPrivate());
        certificateRequestTestFactory.printPEMFormat(certificateOptional.get().getCertificate());
    }
}
