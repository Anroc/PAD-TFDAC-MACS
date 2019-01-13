package de.tuberlin.tfdacmacs.lib.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception that will be thrown by the project.
 * This exception will define a {@link HttpStatus} that will be returned as the status code to the
 * client.
 */
public class ServiceException extends RuntimeException {

    /**
     * status code that will be returned to the client.
     */
    private final HttpStatus httpStatus;

    @Getter
    private boolean printStackTrace = true;

    public ServiceException(HttpStatus httpStatus) {
        super(httpStatus.name());
        this.httpStatus = httpStatus;
    }

    public ServiceException(String formatString, HttpStatus httpStatus,
            Object... args) {
        super(String.format(formatString, args));
        this.httpStatus = httpStatus;
    }

    public ServiceException(String formatString, Throwable cause,
            HttpStatus httpStatus, Object... args) {
        super(String.format(formatString, args), cause);
        this.httpStatus = httpStatus;
    }

    public ServiceException(Throwable cause, HttpStatus httpStatus) {
        super(cause);
        this.httpStatus = httpStatus;
    }

    public ServiceException(String formatString, Throwable e, Object... args) {
        super(String.format(formatString, args), e);
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public ServiceException andDoNotPrintStackTrace() {
        this.printStackTrace = false;
        return this;
    }
}
