package de.tuberlin.tfdacmacs.client.rest.error;

public class SignatureInvalidException extends RuntimeException {

    public SignatureInvalidException(String message) {
        super(message);
    }
}
