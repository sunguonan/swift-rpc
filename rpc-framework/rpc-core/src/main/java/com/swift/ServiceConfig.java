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
    // 添加分组
    private String group = "default";

    public ServiceConfig() {
    }

    public ServiceConfig(Class<?> interfaceProvider, Object implementsProvider) {
        this.interfaceProvider = (Class<T>) interfaceProvider;
        this.implementsProvider = implementsProvider;
    }

    public Class<T> getInterface() {
        return interfaceProvider;
    }

    public void setInterface(Class<?> interfaceProvider) {
        this.interfaceProvider = (Class<T>) interfaceProvider;
    }

    public Object getRef() {
        return implementsProvider;
    }

    public void setRef(Object implementsProvider) {
        this.implementsProvider = implementsProvider;
    }
    
    public String getGroup() {
        return group;
    }
    
    public void setGroup(String group) {
        this.group = group;
    }
}
