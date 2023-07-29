package com.swift.exception;

/**
 * @author sunGuoNan
 * @version 1.0
 */
public class ResponseException extends RuntimeException{

    public ResponseException() {
    }

    public ResponseException(String message) {
        super(message);
    }

    public ResponseException(Throwable cause) {
        super(cause);
    }

    private byte code;
    private String msg;

    public ResponseException(byte code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}
