package com.swift.exception;

/**
 * @author sunGuoNan
 * @version 1.0
 */
public class SerializeException extends RuntimeException {
    private static final long serialVersionUID = -2803982404484222787L;

    public SerializeException() {
    }

    public SerializeException(String message) {
        super(message);
    }

    public SerializeException(Throwable cause) {
        super(cause);
    }
}
