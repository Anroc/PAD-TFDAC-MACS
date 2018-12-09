package de.tuberlin.tfdacmacs.basics.exceptions;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ServiceException {
    public BadRequestException(String formatString, Object... args) {
        super(formatString, HttpStatus.BAD_REQUEST, args);
    }
}
