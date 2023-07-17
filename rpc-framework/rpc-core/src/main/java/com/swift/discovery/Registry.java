package com.swift.discovery;

import com.swift.ServiceConfig;

import java.net.InetSocketAddress;

/**
 * 注册中心
 *
 * @author sunGuoNan
 * @version 1.0
 */
public interface Registry {
    /**
     * 注册服务
     *
     * @param serviceConfig 服务的配置内容
     */
    void register(ServiceConfig<?> serviceConfig);

    /**
     * 从数据中心 拉取一个可用的服务
     *
     * @param name 服务的名称
     * @return 服务的地址
     */
    InetSocketAddress lookup(String name);

}
