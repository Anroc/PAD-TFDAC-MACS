package de.tuberlin.tfdacmacs.crypto.pairing.exceptions;

import de.tuberlin.tfdacmacs.crypto.pairing.data.Versioned;
import lombok.NonNull;

public class VersionMismatchException extends RuntimeException {

    public VersionMismatchException(@NonNull Versioned target, @NonNull Versioned subject) {
        this(target.getVersion(), subject.getVersion());
    }

    public VersionMismatchException(long target, long subject) {
        super(String.format("Could not use key in version %d for target key %d.", target, subject));
    }

    public VersionMismatchException(String message) {
        super(message);
    }
}
