package com.swift.exception;

/**
 * @author sunGuoNan
 * @version 1.0
 */
public class LoadBalancerException extends RuntimeException {
    private static final long serialVersionUID = 7292838398359808576L;

    public LoadBalancerException() {
    }

    public LoadBalancerException(String message) {
        super(message);
    }

    public LoadBalancerException(Throwable cause) {
        super(cause);
    }
}
