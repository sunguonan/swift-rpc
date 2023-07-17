package com.swift.discovery;

import com.swift.ServiceConfig;

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
}
