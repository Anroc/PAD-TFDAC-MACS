package de.tuberlin.tfdacmacs.basics.exceptions;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ServiceException {
    public UnauthorizedException(String formatString, Object... args) {
        super(formatString, HttpStatus.UNAUTHORIZED, args);
    }
}
