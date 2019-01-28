package de.tuberlin.tfdacmacs.client.rest;

import de.tuberlin.tfdacmacs.client.authority.data.dto.AuthorityInformationResponse;

public interface AAClient {

    AuthorityInformationResponse getTrustedAuthorities();

}
