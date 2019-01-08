package de.tuberlin.tfdacmacs.client.rest.error;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

public class InterServiceCallError extends RuntimeException {

    public InterServiceCallError(HttpMethod httpMethod, String url, HttpStatus responseStatus) {
        super(String.format(
                "Error requesting [%s:%s]: Response status was [%s].",
                httpMethod.toString(),
                url,
                responseStatus
        ));
    }
}
