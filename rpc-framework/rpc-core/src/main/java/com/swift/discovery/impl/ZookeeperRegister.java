package com.swift.discovery.impl;

import com.swift.Constant;
import com.swift.ServiceConfig;
import com.swift.discovery.AbstractRegister;
import com.swift.util.NetUtils;
import com.swift.util.zookeeper.ZookeeperNode;
import com.swift.util.zookeeper.ZookeeperUtil;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

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
        String localNode = parentNode + "/" + NetUtils.getIp() + ":" + 8080;
        ZookeeperNode zookeeperLocalNode = new ZookeeperNode(localNode, null);
        // 发布结点  这个节点是一个临时节点
        if (!ZookeeperUtil.exists(zooKeeper, localNode, null)) {
            ZookeeperUtil.createNode(zooKeeper, zookeeperLocalNode, null, CreateMode.EPHEMERAL);
        }
    }
}
