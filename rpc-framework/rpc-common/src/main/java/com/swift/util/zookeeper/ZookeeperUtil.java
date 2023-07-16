package com.swift.util.zookeeper;

import com.swift.Constant;
import com.swift.exception.ZookeeperException;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author sunGuoNan
 * @version 1.0
 */
@Slf4j
public class ZookeeperUtil {

    /**
     * 创建与zookeeper的连接
     *
     * @return ZooKeeper实例
     */
    public static ZooKeeper createZookeeper() {
        // 定义连接参数
        String connectString = Constant.DEFAULT_ZK_CONNECT;
        int sessionTimeout = Constant.DEFAULT_ZK_Session_Timeout;
        return createZookeeper(connectString, sessionTimeout);
    }

    /**
     * 创建与zookeeper的连接
     *
     * @param connectString  zookeeper地址
     * @param sessionTimeout 连接的超时时间
     * @return ZooKeeper实例
     */
    public static ZooKeeper createZookeeper(String connectString, int sessionTimeout) {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        try {
            // 创建zookeeper实例 建立连接
            final ZooKeeper zooKeeper = new ZooKeeper(connectString, sessionTimeout, event -> {
                // 只有连接成功才放行
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    log.debug("客户端连接成功");
                    countDownLatch.countDown();
                }
            });
            // 线程会等待计数器减为0后才放行
            countDownLatch.await();
            return zooKeeper;
        } catch (IOException | InterruptedException e) {
            log.error("与zookeeper建立连接失败", e);
            throw new ZookeeperException();
        }
    }


    /**
     * @param zooKeeper  zooKeeper 实例
     * @param node       结点
     * @param watcher    监听器
     * @param createMode 结点类型
     * @return true 成功创建 false 已经存在  异常 直接抛出
     */
    public static Boolean createNode(ZooKeeper zooKeeper, ZookeeperNode node,
                                     Watcher watcher, CreateMode createMode) {
        try {
            if (zooKeeper.exists(node.getNodePath(), watcher) == null) {
                String result = zooKeeper.create(node.getNodePath(), node.getData(),
                        ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
                log.debug("zookeeper创建结点为：{}", result);
                return true;
            } else {
                log.info("结点已经存在无需创建{}", node.getNodePath());
                return false;
            }
        } catch (KeeperException | InterruptedException e) {
            log.error("创建基础目录发生异常", e);
            throw new ZookeeperException(e);
        }
    }
    
    public static void close(ZooKeeper zooKeeper) {
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            log.error("关闭zookeeper连接有问题", e);
            throw new ZookeeperException(e);
        }
    }
}
