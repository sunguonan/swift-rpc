package com.swift.discovery;

import com.swift.ServiceConfig;

import java.net.InetSocketAddress;
import java.util.List;

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
     * 从数据中心 拉取服务列表
     *
     * @param name 服务的名称
     * @return 服务的地址
     */
    List<InetSocketAddress> lookup(String name,String group);

}
