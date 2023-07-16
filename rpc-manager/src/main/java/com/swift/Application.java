package com.swift;

import com.swift.util.zookeeper.ZookeeperNode;
import com.swift.util.zookeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

/**
 * 自动创建zookeeper基础目录
 *
 * @author sunGuoNan
 * @version 1.0
 */
@Slf4j
public class Application {
    public static void main(String[] args) {
        // 建立与zookeeper的连接
        ZooKeeper zookeeper = ZookeeperUtil.createZookeeper();

        // 定义zookeeper路径地址
        String basePath = "/rpc-metadata";
        String providerPath = basePath + "/providers";
        String consumerPath = basePath + "/consumers";
        ZookeeperNode baseNode = new ZookeeperNode(basePath, null);
        ZookeeperNode providerNode = new ZookeeperNode(providerPath, null);
        ZookeeperNode consumerNode = new ZookeeperNode(consumerPath, null);

        // 创建结点
        List.of(baseNode, providerNode, consumerNode).forEach(node ->
                ZookeeperUtil.createNode(zookeeper, node, null, CreateMode.PERSISTENT));

        // 关闭zookeeper连接
        ZookeeperUtil.close(zookeeper);
    }
}
