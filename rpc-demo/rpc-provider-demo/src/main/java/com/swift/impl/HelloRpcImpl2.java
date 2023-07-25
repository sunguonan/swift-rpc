package com.swift.impl;

import com.swift.HelloRpc2;
import com.swift.annotation.RpcApi;


/**
 * @author sunGuoNan
 * @version 1.0
 */
@RpcApi
public class HelloRpcImpl2 implements HelloRpc2 {
    @Override
    public String sayHi(String msg) {
        return "hi consumer" + msg;
    }
}
