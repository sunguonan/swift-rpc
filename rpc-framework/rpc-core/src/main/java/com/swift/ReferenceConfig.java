package com.swift;

import com.swift.discovery.Registry;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

/**
 * @author sunGuoNan
 * @version 1.0
 */
public class ReferenceConfig<T> {
    private Class<T> interfaceConsumer;

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
        // 使用动态代理生成代理对象  代理对象 --> 发送请求 处理远程调用的内容
        Object proxy = Proxy.newProxyInstance(classLoader, interfaces, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // 调用sayHi方法会走入到这个代码段中
                // method 获取具体方法  args 获取参数列表

                // 1. 从注册中心找到一个可用的服务
                InetSocketAddress inetSocketAddress = registry.lookup(interfaceConsumer.getName());
                // 2. 使用netty连接服务器 发送服务的名字 方法名字 参数列表  得到最后的结果

                return null;
            }
        });
        return (T) proxy;
    }
}
