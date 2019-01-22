package de.tuberlin.tfdacmacs.client.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GeneralExceptionHandler {

    @ExceptionHandler(Exception.class)
    public void handleException(Exception e) {
        log.error("{}", e.getMessage(), e);
    }
}
