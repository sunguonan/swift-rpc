package com.swift.exception;

/**
 * @author sunGuoNan
 * @version 1.0
 */
public class CompressException extends RuntimeException {

    private static final long serialVersionUID = -5885826867826644219L;

    public CompressException() {
    }

    public CompressException(String message) {
        super(message);
    }

    public CompressException(Throwable cause) {
        super(cause);
    }
}
