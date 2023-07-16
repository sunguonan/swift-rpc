package com.swift;

/**
 * 封装需要发布的服务
 *
 * @author sunGuoNan
 * @version 1.0
 */
public class RegisterConfig {
    // 连接名称
    private String connectString;

    public RegisterConfig() {
    }

    public RegisterConfig(String connectString) {
        this.connectString = connectString;
    }

}
