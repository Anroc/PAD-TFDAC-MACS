package de.tuberlin.tfdacmacs.client.rest;

import de.tuberlin.tfdacmacs.client.authority.client.dto.AuthorityInformationResponse;
import de.tuberlin.tfdacmacs.client.twofactor.client.dto.DeviceIdResponse;

import java.util.Optional;

public interface AAClient {

    AuthorityInformationResponse getTrustedAuthorities();
    Optional<DeviceIdResponse> getDevice(String userId, String deviceId);
}
