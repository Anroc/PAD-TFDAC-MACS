package de.tuberlin.tfdacmacs.attributeauthority.authority;

import de.tuberlin.tfdacmacs.RestTestSuite;
import de.tuberlin.tfdacmacs.attributeauthority.authority.data.TrustedAuthority;
import de.tuberlin.tfdacmacs.attributeauthority.authority.data.dto.AuthorityInformationResponse;
import de.tuberlin.tfdacmacs.attributeauthority.authority.data.dto.TrustedAuthorityCreationRequest;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.User;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthorityRestTest extends RestTestSuite {

    private TrustedAuthority trustedAuthority;
    private User user;

    @Before
    public void setup() {
        trustedAuthority = new TrustedAuthority(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );

        trustedAuthorityDB.insert(trustedAuthority);

        user = new User("test@tu-berlin.de");

        userDB.insert(user);
    }

    @Test
    public void getAuthorityInformation() {
        ResponseEntity<AuthorityInformationResponse> exchange = mutualAuthRestTemplate
                .exchange("/authority", HttpMethod.GET, HttpEntity.EMPTY, AuthorityInformationResponse.class);

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        AuthorityInformationResponse body = exchange.getBody();
        assertThat(body.getCertificateId()).isNotBlank();
        assertThat(body.getId()).isEqualTo(attributeAuthorityConfig.getId());
        assertThat(body.getTrustedAuthorityIds()).hasSize(1).containsEntry(
                trustedAuthority.getId(),
                trustedAuthority.getCertificateId()
        );
    }

    @Test
    public void createTrustedAuthority() {
        TrustedAuthorityCreationRequest request = new TrustedAuthorityCreationRequest(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );

        ResponseEntity<AuthorityInformationResponse> exchange = sslRestTemplate
                .exchange("/authority/trusted-authority", HttpMethod.POST, new HttpEntity<>(request, basicAuth()),
                        AuthorityInformationResponse.class);

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.CREATED);
        AuthorityInformationResponse body = exchange.getBody();
        assertThat(body.getCertificateId()).isNotBlank();
        assertThat(body.getId()).isEqualTo(attributeAuthorityConfig.getId());
        assertThat(body.getTrustedAuthorityIds()).hasSize(2)
                .containsEntry(
                    trustedAuthority.getId(),
                    trustedAuthority.getCertificateId())
                .containsEntry(
                        request.getId(),
                        request.getCertificateId()
                );
    }

    @Test
    public void deleteTrustedAuthority() {
        ResponseEntity<AuthorityInformationResponse> exchange = sslRestTemplate
                .exchange("/authority/" + trustedAuthority.getId(), HttpMethod.DELETE, new HttpEntity<>(basicAuth()),
                        AuthorityInformationResponse.class);

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        AuthorityInformationResponse body = exchange.getBody();
        assertThat(body.getCertificateId()).isNotBlank();
        assertThat(body.getId()).isEqualTo(attributeAuthorityConfig.getId());
        assertThat(body.getTrustedAuthorityIds()).isEmpty();
    }
}
