package com.swift;

/**
 * 服务提供方提供对应的服务
 * 服务里面包含 需要调用的接口和具体实现
 *
 * @author sunGuoNan
 * @version 1.0
 */
public class ServiceConfig<T> {
    // 提供服务的接口
    private Class<T> interfaceProvider;
    // 具体被调用接口的实现
    private Object implementsProvider;

    public ServiceConfig() {
    }

    public ServiceConfig(Class<T> interfaceProvider, Object implementsProvider) {
        this.interfaceProvider = interfaceProvider;
        this.implementsProvider = implementsProvider;
    }

    public Class<T> getInterface() {
        return interfaceProvider;
    }

    public void setInterface(Class<T> interfaceProvider) {
        this.interfaceProvider = interfaceProvider;
    }

    public Object getRef() {
        return implementsProvider;
    }

    public void setRef(Object implementsProvider) {
        this.implementsProvider = implementsProvider;
    }
}
