package de.tuberlin.tfdacmacs.client.rest.template;

import de.tuberlin.tfdacmacs.client.authority.data.dto.AuthorityInformationResponse;
import de.tuberlin.tfdacmacs.client.rest.AAClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class AAClientTemplate extends ClientRestTemplate implements AAClient {

    public AAClientTemplate(@Qualifier(RestTemplateFactory.AA_REST_TEMPLATE_BEAN_NAME) RestTemplate restTemplate) {
        super(restTemplate, "AA");
    }

    @Override
    public AuthorityInformationResponse getTrustedAuthorities() {
        return request(
                "/authority",
                HttpMethod.GET,
                AuthorityInformationResponse.class,
                null
        );
    }
}
