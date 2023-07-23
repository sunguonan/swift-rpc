package com.swift.loadbalancer;

import java.net.InetSocketAddress;

/**
 * 负载均衡算法选择器接口
 *
 * @author sunGuoNan
 * @version 1.0
 */
public interface Selector {

    /**
     * 根据服务列表执行一种算法获取一个服务节点
     *
     * @return 具体的服务节点
     */
    InetSocketAddress getNext();

}
