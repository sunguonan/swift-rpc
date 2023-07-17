package com.swift.exception;

/**
 * @author sunGuoNan
 * @version 1.0
 */
public class DiscoveryException extends RuntimeException {
    private static final long serialVersionUID = -6991557390263430272L;

    public DiscoveryException() {
    }

    public DiscoveryException(String message) {
        super(message);
    }

    public DiscoveryException(Throwable cause) {
        super(cause);
    }
}
