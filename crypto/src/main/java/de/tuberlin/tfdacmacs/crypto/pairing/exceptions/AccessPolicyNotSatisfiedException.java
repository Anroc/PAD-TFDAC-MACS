package de.tuberlin.tfdacmacs.crypto.pairing.exceptions;

public class AccessPolicyNotSatisfiedException extends RuntimeException {

    public AccessPolicyNotSatisfiedException() {
    }

    public AccessPolicyNotSatisfiedException(String message) {
        super(message);
    }

    public AccessPolicyNotSatisfiedException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccessPolicyNotSatisfiedException(Throwable cause) {
        super(cause);
    }
}
