package com.swift;

/**
 * @author sunGuoNan
 * @version 1.0
 */
public class HelloRpcImpl implements HelloRpc {
    @Override
    public String sayHi(String msg) {
        return "hi consumer" + msg;
    }
}
