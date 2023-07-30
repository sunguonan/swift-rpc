package com.swift.loadbalancer;


import com.swift.RpcBootStrap;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sunGuoNan
 * @version 1.0
 */
public abstract class AbstractLoadBalancer implements LoadBalancer {

    // 一个服务匹配一个算法选择器
    private static final Map<String, Selector> CACHE = new ConcurrentHashMap<>(8);

    @Override
    public InetSocketAddress selectServiceAddress(String serviceName,String group) {
        // 1. 优先从cache中获取一个选择器
        Selector selector = CACHE.get(serviceName);

        // 2. 如果没有，就需要为这个service创建一个selector
        if (selector == null) {
            // 找到可用服务列表
            List<InetSocketAddress> serviceList = 
                    RpcBootStrap.getInstance().getConfiguration().getRegistryConfig().getRegister().lookup(serviceName,group);

            // 使用算法选择合适的结点
            selector = getSelector(serviceList);

            // 将select放入缓存当中
            CACHE.put(serviceName, selector);
        }

        // 获取可用节点
        return selector.getNext();
    }

    @Override
    public synchronized void reLoadBalance(String serviceName, List<InetSocketAddress> addresses) {
        // 我们可以根据新的服务列表生成新的selector
        CACHE.put(serviceName, getSelector(addresses));
    }

    /**
     * 由子类进行扩展 负载均衡选择器
     *
     * @param serviceList 服务列表
     * @return 负载均衡算法选择器
     */
    protected abstract Selector getSelector(List<InetSocketAddress> serviceList);

}
