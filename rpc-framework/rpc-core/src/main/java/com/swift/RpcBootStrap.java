package com.swift;

import com.swift.discovery.RegisterConfig;
import com.swift.discovery.Registry;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private String appName = "default";
    private RegisterConfig registerConfig;
    private ProtocolConfig protocolConfig;
    private Registry registry;
    // 维护暴露的服务列表  key --> interface的全限定名称 value ServiceConfig
    private static final Map<String, ServiceConfig<?>> SERVICE_LIST = new ConcurrentHashMap<>(16);

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
        this.appName = appName;
        return this;
    }

    /**
     * 配置注册中心
     *
     * @param registerConfig 注册中心
     * @return this 对象实例
     */
    public RpcBootStrap registry(RegisterConfig registerConfig) {
        // 使用registerConfig获取一个配置中心  --- 简单工厂设计模式
        this.registry = registerConfig.getRegister();
        return this;
    }

    /**
     * 配置序列化协议
     *
     * @param protocolConfig 序列化协议  eg 默认jdk
     * @return this 对象实例
     */
    public RpcBootStrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
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
    public RpcBootStrap publish(ServiceConfig<?> server) {
        // 抽象出注册中心的概念 使用注册中心的实现完成注册
        registry.register(server);

        // 当服务调用方 通过方法名和参数进行方法调用 怎么提供哪一个实现
        // 1. new一个 2. spring bean工厂  3. 自己维护映射关系
        SERVICE_LIST.put(server.getInterface().getName(), server);

        return this;
    }

    /**
     * 批量发布
     *
     * @param server 封装需要发布的服务
     * @return this 对象实例
     */
    public RpcBootStrap publish(List<ServiceConfig<?>> server) {
        for (ServiceConfig<?> serviceConfig : server) {
            this.publish(serviceConfig);
        }
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {
        try {
            Thread.sleep(1000000000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 在这个方法中 可以拿到相关配置项  eg 注册中心
     * 那么就可以拿到Reference --> 在将来调用get方法时 生成代理对象
     *
     * @param reference 封装需要发布的服务
     * @return RpcBootStrap 实例
     */
    public RpcBootStrap reference(ReferenceConfig<?> reference) {
        // 获取注册中心
        reference.setRegistry(registry);
        return this;
    }
}
