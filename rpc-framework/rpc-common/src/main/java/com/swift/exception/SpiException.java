package com.swift.exception;

/**
 * @author sunGuoNan
 * @version 1.0
 */
public class SpiException extends RuntimeException{
    public SpiException() {
    }

    public SpiException(String message) {
        super(message);
    }

    public SpiException(Throwable cause) {
        super(cause);
    }
}
