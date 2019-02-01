package de.tuberlin.tfdacmacs.client.rest.template;

import de.tuberlin.tfdacmacs.client.authority.client.dto.AuthorityInformationResponse;
import de.tuberlin.tfdacmacs.client.rest.AAClient;
import de.tuberlin.tfdacmacs.client.twofactor.client.dto.DeviceIdResponse;
import de.tuberlin.tfdacmacs.client.rest.error.InterServiceCallError;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

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

    @Override
    public Optional<DeviceIdResponse> getDevice(@NonNull String userId, @NonNull String deviceId) {
        try {
            return Optional.of(
                    request(
                            String.format("/users/%s/devices/%s", userId, deviceId),
                            HttpMethod.GET,
                            DeviceIdResponse.class,
                            null
                    )
            );
        } catch (InterServiceCallError e) {
            if(e.getResponseStatus() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            } else {
                throw e;
            }
        }
    }
}
