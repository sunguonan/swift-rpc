package com.swift;

import com.swift.discovery.Registry;
import com.swift.proxy.handlerrpc.ConsumerInvocationHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;

/**
 * 服务调用者封装具体需要调用那个接口下的方法
 *
 * @author sunGuoNan
 * @version 1.0
 */
@Slf4j
public class ReferenceConfig<T> {
    // 需要远程调用的接口的具体实现
    private Class<T> interfaceConsumer;
    // 注册中心
    private Registry registry;

    public ReferenceConfig() {
    }

    public ReferenceConfig(Class<T> interfaceConsumer) {
        this.interfaceConsumer = interfaceConsumer;
    }

    public Class<T> getInterface() {
        return interfaceConsumer;
    }

    public void setInterface(Class<T> interfaceConsumer) {
        this.interfaceConsumer = interfaceConsumer;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    /**
     * 使用代理设计模式 生成一个api接口的代理对象
     *
     * @return 代理对象
     */
    public T get() {
        // 使用动态代理完成事情
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<?>[] interfaces = {interfaceConsumer};
        ConsumerInvocationHandler consumerInvocationHandler = new ConsumerInvocationHandler(registry, interfaceConsumer);
        // 使用动态代理生成代理对象  代理对象 --> 发送请求 处理远程调用的内容
        Object proxy = Proxy.newProxyInstance(classLoader, interfaces, consumerInvocationHandler);
        return (T) proxy;
    }
}
