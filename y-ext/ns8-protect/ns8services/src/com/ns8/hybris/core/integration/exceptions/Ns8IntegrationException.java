package com.ns8.hybris.core.integration.exceptions;


import org.springframework.http.HttpStatus;

/**
 * Exception thrown from the NS8 integration errors
 */
public class Ns8IntegrationException extends RuntimeException {

    private final HttpStatus httpStatus;

    /**
     * Constructor
     *
     * @param message    exception message
     * @param httpStatus http status
     */
    public Ns8IntegrationException(final String message, final HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    /**
     * Constructor
     *
     * @param message    exception message
     * @param httpStatus http status
     * @param throwable  the throwable to pass
     */
    public Ns8IntegrationException(final String message, final HttpStatus httpStatus, final Throwable throwable) {
        super(message, throwable);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
