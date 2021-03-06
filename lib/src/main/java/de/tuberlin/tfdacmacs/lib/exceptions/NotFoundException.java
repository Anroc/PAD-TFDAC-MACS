package de.tuberlin.tfdacmacs.lib.exceptions;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ServiceException {

    public NotFoundException(String id) {
        super("Entity with id [%s] not found.", HttpStatus.NOT_FOUND, id);
    }
}
