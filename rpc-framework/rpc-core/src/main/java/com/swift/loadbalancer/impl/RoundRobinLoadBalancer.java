package com.swift.loadbalancer.impl;


import com.swift.exception.LoadBalancerException;
import com.swift.loadbalancer.AbstractLoadBalancer;
import com.swift.loadbalancer.Selector;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询的负载均衡策略
 *
 * @author sunGuoNan
 * @version 1.0
 */
@Slf4j
public class RoundRobinLoadBalancer extends AbstractLoadBalancer {

    /**
     * 选择一个负载均衡算法
     *
     * @param serviceList 服务列表
     * @return Selector选择器
     */
    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new RoundRobinSelector(serviceList);
    }

    /**
     * 负载均衡策略内部维护一个选择器 静态内部类
     */
    private static class RoundRobinSelector implements Selector {
        private final List<InetSocketAddress> serviceList;
        private final AtomicInteger index;

        public RoundRobinSelector(List<InetSocketAddress> serviceList) {
            this.serviceList = serviceList;
            this.index = new AtomicInteger(0);
        }

        @Override
        public InetSocketAddress getNext() {
            if (serviceList == null || serviceList.size() == 0) {
                log.error("进行负载均衡选取节点时发现服务列表为空.");
                throw new LoadBalancerException();
            }

            // 获取轮询结点
            InetSocketAddress address = serviceList.get(index.get());

            // 如果他到了最后的一个位置  重置
            if (index.get() == serviceList.size() - 1) {
                index.set(0);
            } else {
                // 游标后移一位
                index.incrementAndGet();
            }

            return address;
        }
    }

}
