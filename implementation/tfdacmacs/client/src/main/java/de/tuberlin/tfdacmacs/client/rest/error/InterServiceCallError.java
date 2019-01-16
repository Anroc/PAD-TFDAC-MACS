package de.tuberlin.tfdacmacs.client.rest.error;

import lombok.Data;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@Data
public class InterServiceCallError extends RuntimeException {

    private final HttpStatus responseStatus;

    public InterServiceCallError(HttpMethod httpMethod, String url, HttpStatus responseStatus) {
        super(errorMessage(httpMethod, url, responseStatus));
        this.responseStatus = responseStatus;
    }

    public InterServiceCallError(HttpMethod httpMethod, String url, HttpStatus responseStatus, Throwable cause) {
        super(errorMessage(httpMethod, url, responseStatus), cause);
        this.responseStatus = responseStatus;
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
