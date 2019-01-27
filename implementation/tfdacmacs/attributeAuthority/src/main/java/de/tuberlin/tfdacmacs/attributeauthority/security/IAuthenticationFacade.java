package de.tuberlin.tfdacmacs.attributeauthority.security;

import org.springframework.security.core.Authentication;

public interface IAuthenticationFacade {
    Authentication getAuthentication();
}