package de.tuberlin.tfdacmacs.lib.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Exception mapper that handles the {@link ServiceException}.
 */
@Slf4j
@ControllerAdvice
public class ServiceExceptionHandler {

    /**
     * Handles the given {@link ServiceException}.
     * Logs the error and defines the status code.
     *
     * @param serviceException the exception that was raised.
     * @param response         the {@link HttpServletResponse} injected by spring
     * @throws IOException if thrown by the {@link HttpServletResponse#sendError(int)} function.
     */
    @ExceptionHandler(ServiceException.class)
    public void handleServiceException(ServiceException serviceException, HttpServletResponse response)
            throws IOException {

        if(serviceException.isPrintStackTrace()) {
            log.error(serviceException.getMessage(), serviceException);
        } else {
            log.info(serviceException.getMessage());
        }
        response.sendError(serviceException.getHttpStatus().value(), serviceException.getMessage());
    }

}
