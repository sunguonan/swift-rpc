package com.swift;

/**
 * @author sunGuoNan
 * @version 1.0
 */
public class Constant {
    /**
     * zookeeper默认连接地址
     */
    public static final String DEFAULT_ZK_CONNECT = "127.0.0.1:2181";

    /**
     * zookeeper默认连接超时时间
     */
    public static final int DEFAULT_ZK_Session_Timeout = 10000;

    /**
     * 提供者基础路径
     */
    public static final String BASE_PROVIDER_PATH = "/rpc-metadata/providers";

    /**
     * 消费者基础路径
     */
    public static final String BASE_CONSUMER_PATH = "/rpc-metadata/consumers";
}
