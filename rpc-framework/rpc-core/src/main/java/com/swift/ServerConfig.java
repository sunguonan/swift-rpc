package com.swift;

/**
 * @author sunGuoNan
 * @version 1.0
 */
public class ServerConfig<T> {

    private Class<T> interfaceProvider;
    private Object implementsProvider;

    public ServerConfig() {
    }

    public ServerConfig(Class<T> interfaceProvider, Object implementsProvider) {
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
