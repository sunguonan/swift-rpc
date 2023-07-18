package com.swift.exception;

/**
 * @author sunGuoNan
 * @version 1.0
 */
public class NetworkException extends RuntimeException {
    private static final long serialVersionUID = -6621119290204656019L;

    public NetworkException() {
    }

    public NetworkException(String message) {
        super(message);
    }

    public NetworkException(Throwable cause) {
        super(cause);
    }
}
