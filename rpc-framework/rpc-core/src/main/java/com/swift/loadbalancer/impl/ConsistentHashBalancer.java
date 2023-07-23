package com.swift.loadbalancer.impl;


import com.swift.RpcBootStrap;
import com.swift.loadbalancer.AbstractLoadBalancer;
import com.swift.loadbalancer.Selector;
import com.swift.transport.message.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 一致性hash负载均衡策略
 *
 * @author sunGuoNan
 * @version 1.0
 */
@Slf4j
public class ConsistentHashBalancer extends AbstractLoadBalancer {

    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new ConsistentHashSelector(serviceList, 128);
    }

    /**
     * 一致性hash的具体算法实现
     */
    private static class ConsistentHashSelector implements Selector {

        // hash环用来存储服务器节点
        private SortedMap<Integer, InetSocketAddress> circle = new TreeMap<>();
        // 虚拟节点的个数
        private int virtualNodes;

        public ConsistentHashSelector(List<InetSocketAddress> serviceList, int virtualNodes) {
            // 我们应该尝试将节点转化为虚拟节点，进行挂载
            this.virtualNodes = virtualNodes;
            for (InetSocketAddress inetSocketAddress : serviceList) {
                // 需要把每一个节点加入到hash环中
                addNodeToCircle(inetSocketAddress);
            }
        }

        @Override
        public InetSocketAddress getNext() {
            // 1、hash环已经建立好了，接下来需要对请求的要素做处理我们应该选择什么要素来进行hash运算
            // 有没有办法可以获取，到具体的请求内容  --> threadLocal
            RpcRequest rpcRequest = RpcBootStrap.REQUEST_THREAD_LOCAL.get();

            // 我们想根据请求的一些特征来选择服务器  id
            String requestId = Long.toString(rpcRequest.getRequestId());

            // 请求的id做hash，字符串默认的hash不太好
            int hash = hash(requestId);

            // 判断该hash值是否能直接落在一个服务器上，和服务器的hash一样
            if (!circle.containsKey(hash)) {
                // 寻找理我最近的一个节点
                SortedMap<Integer, InetSocketAddress> tailMap = circle.tailMap(hash);
                hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
            }

            return circle.get(hash);
        }

        /**
         * 将每个节点挂载到hash环上
         *
         * @param inetSocketAddress 节点的地址
         */
        private void addNodeToCircle(InetSocketAddress inetSocketAddress) {
            // 为每一个节点生成匹配的虚拟节点进行挂载
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(inetSocketAddress.toString() + "-" + i);
                // 关在到hash环上
                circle.put(hash, inetSocketAddress);
                if (log.isDebugEnabled()) {
                    log.debug("hash为[{}]的节点已经挂载到了哈希环上.", hash);
                }
            }
        }

        private void removeNodeFromCircle(InetSocketAddress inetSocketAddress) {
            // 为每一个节点生成匹配的虚拟节点进行挂载
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(inetSocketAddress.toString() + "-" + i);
                // 关在到hash环上
                circle.remove(hash);
            }
        }

        /**
         * 具体的hash算法, 这样也是不均匀的
         *
         * @param s
         * @return
         */
        private int hash(String s) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            byte[] digest = md.digest(s.getBytes());
            // md5得到的结果是一个字节数组，但是我们想要int 4个字节

            int res = 0;
            for (int i = 0; i < 4; i++) {
                res = res << 8;
                if (digest[i] < 0) {
                    res = res | (digest[i] & 255);
                } else {
                    res = res | digest[i];
                }
            }
            return res;
        }

        private String toBinary(int i) {
            String s = Integer.toBinaryString(i);
            int index = 32 - s.length();
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < index; j++) {
                sb.append(0);
            }
            sb.append(s);
            return sb.toString();
        }
    }


}
