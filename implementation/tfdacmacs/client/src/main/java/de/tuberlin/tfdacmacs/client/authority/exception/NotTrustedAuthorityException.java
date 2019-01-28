package de.tuberlin.tfdacmacs.client.authority.exception;

public class NotTrustedAuthorityException extends RuntimeException {

    public NotTrustedAuthorityException(String authorityId) {
        super(String.format("AuthorityId %s is not trusted.", authorityId));
    }
}
