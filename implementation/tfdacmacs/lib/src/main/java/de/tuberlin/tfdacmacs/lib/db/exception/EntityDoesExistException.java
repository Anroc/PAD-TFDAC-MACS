package de.tuberlin.tfdacmacs.lib.db.exception;

public class EntityDoesExistException extends DBException {
    public EntityDoesExistException() {
    }

    public EntityDoesExistException(String message) {
        super(message);
    }

    public EntityDoesExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntityDoesExistException(Throwable cause) {
        super(cause);
    }

    public EntityDoesExistException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
