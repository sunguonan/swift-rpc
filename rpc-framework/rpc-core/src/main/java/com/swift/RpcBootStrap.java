package com.swift;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 核心的引导程序
 *
 * @author sunGuoNan
 * @version 1.0
 */
@Slf4j
public class RpcBootStrap {
    /**
     * RpcBootStrap是个单例  只希望每个应用程序只有一个实例
     * 单例 --> 懒汉式  私有化构造器  别人不能new
     */
    private static final RpcBootStrap rpcBootStrap = new RpcBootStrap();

    private RpcBootStrap() {
        // 私有化构造器  做一些初始化的事情
    }

    /**
     * 获取实例对象
     *
     * @return this 对象实例
     */
    public static RpcBootStrap getInstance() {
        return rpcBootStrap;
    }

    /**
     * 配置应用的名称
     *
     * @param appName 应用名称
     * @return this 对象实例
     */
    public RpcBootStrap application(String appName) {
        return this;
    }

    /**
     * 配置注册中心
     *
     * @param RegisterConfig 注册中心
     * @return this 对象实例
     */
    public RpcBootStrap registry(RegisterConfig RegisterConfig) {
        return this;
    }

    /**
     * 配置序列化协议
     *
     * @param protocol 序列化协议  eg 默认jdk
     * @return this 对象实例
     */
    public RpcBootStrap protocol(ProtocolConfig protocol) {
        return this;
    }

    /**
     * 配置发布服务
     * 将需要发布服务的接口以及对应的实现 注册到服务中心
     * 单个发布
     *
     * @param server 封装需要发布的服务
     * @return this 对象实例
     */
    public RpcBootStrap publish(ServerConfig<?> server) {
        return this;
    }

    /**
     * 批量发布
     *
     * @param server 封装需要发布的服务
     * @return this 对象实例
     */
    public RpcBootStrap publish(List<ServerConfig<?>> server) {
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {

    }

    /**
     * 在这个方法中 可以拿到相关配置项  eg 注册中心
     * 那么就可以拿到Reference --> 在将来调用get方法时 生成代理对象
     *
     * @param reference 封装需要发布的服务
     */
    public void reference(ReferenceConfig<?> reference) {

    }
}
