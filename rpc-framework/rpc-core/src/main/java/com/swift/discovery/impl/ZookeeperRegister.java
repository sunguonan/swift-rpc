package com.swift.discovery.impl;

import com.swift.Constant;
import com.swift.RpcBootStrap;
import com.swift.ServiceConfig;
import com.swift.discovery.AbstractRegister;
import com.swift.exception.DiscoveryException;
import com.swift.util.NetUtils;
import com.swift.util.zookeeper.ZookeeperNode;
import com.swift.util.zookeeper.ZookeeperUtil;
import com.swift.watch.UpAndDownWatcher;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 使用zookeeper作为注册中心
 *
 * @author sunGuoNan
 * @version 1.0
 */
public class ZookeeperRegister extends AbstractRegister {

    private final ZooKeeper zooKeeper;

    public ZookeeperRegister() {
        this.zooKeeper = ZookeeperUtil.createZookeeper();
    }

    public ZookeeperRegister(String connectString, int timeout) {
        this.zooKeeper = ZookeeperUtil.createZookeeper(connectString, timeout);
    }

    @Override
    public void register(ServiceConfig<?> service) {
        // 创建服务节点
        String parentNode = Constant.BASE_PROVIDER_PATH + "/" + service.getInterface().getName();
        ZookeeperNode zookeeperParentNode = new ZookeeperNode(parentNode, null);
        // 发布结点  这个结点是一个持久的结点
        if (!ZookeeperUtil.exists(zooKeeper, parentNode, null)) {
            ZookeeperUtil.createNode(zooKeeper, zookeeperParentNode, null, CreateMode.PERSISTENT);
        }

        // 创建本机结点  以ip:port的形式  该结点是临时结点
        // 服务提供的端口一般由自己设置
        // ip一般是局域网地址  不是127.0.0.1
        // TODO port  写死 8080  后面改
        String localNode = parentNode + "/" + NetUtils.getIp() + ":" + RpcBootStrap.PORT;
        ZookeeperNode zookeeperLocalNode = new ZookeeperNode(localNode, null);
        // 发布结点  这个节点是一个临时节点
        if (!ZookeeperUtil.exists(zooKeeper, localNode, null)) {
            ZookeeperUtil.createNode(zooKeeper, zookeeperLocalNode, null, CreateMode.EPHEMERAL);
        }
    }

    @Override
    public List<InetSocketAddress> lookup(String serviceName) {
        // 1、找到服务对应的节点
        String serviceNode = Constant.BASE_PROVIDER_PATH + "/" + serviceName;
        // 2. 从zookeeper中获取子节点
        List<String> children = ZookeeperUtil.getChildren(zooKeeper, serviceNode, new UpAndDownWatcher());
        List<InetSocketAddress> inetSocketAddresses = children.stream().map(ipString -> {
            String[] ipAndPort = ipString.split(":");
            String ip = ipAndPort[0];
            int port = Integer.parseInt(ipAndPort[1]);
            return new InetSocketAddress(ip, port);
        }).collect(Collectors.toList());

        if (inetSocketAddresses.size() == 0) {
            throw new DiscoveryException("未发现任何可用的服务主机.");
        }

        return inetSocketAddresses;
    }
}
