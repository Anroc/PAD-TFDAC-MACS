package de.tuberlin.tfdacmacs.client.rest.error;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

public class InterServiceCallError extends RuntimeException {

    public InterServiceCallError(HttpMethod httpMethod, String url, HttpStatus responseStatus) {
        super(errorMessage(httpMethod, url, responseStatus));
    }

    public InterServiceCallError(HttpMethod httpMethod, String url, HttpStatus responseStatus, Throwable cause) {
        super(errorMessage(httpMethod, url, responseStatus), cause);
    }

    private static String errorMessage(HttpMethod httpMethod, String url, HttpStatus responseStatus) {
        return String.format(
                "Error requesting [%s:%s]: Response status was [%s].",
                httpMethod.toString(),
                url,
                responseStatus
        );
    }
}
