package de.tuberlin.tfdacmacs.client.authority.exception;

public class InvalidAuthorityIdentifier extends RuntimeException {
    public InvalidAuthorityIdentifier(String authorityId) {
        super("Could not find authority with id: " + authorityId);
    }
}
