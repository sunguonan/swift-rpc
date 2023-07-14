package com.swift;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author sunGuoNan
 * @version 1.0
 */
public class ZookeeperTest {

    private ZooKeeper zooKeeper;

    @Before
    public void testBefore() {
        String connectString = "127.0.0.1";
        int sessionTimeout = 5000;

        try {
            // 创建和zookeeper的连接
            // String connectString,  创建结点名称
            // int sessionTimeout,  连接的超时时间
            // Watcher watcher 事件监听器
            zooKeeper = new ZooKeeper(connectString, sessionTimeout, new MyWatcher());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateZookeeper() {
        // final String path,  创建路径
        // byte[] data,   数据
        // List<ACL> acl,  访问控制权限
        // CreateMode createMode  模式选择 eg 是否持久化等
        try {
            String result = zooKeeper.create("/ydlclass", "hello".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            System.out.println(result);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (zooKeeper != null) {
                try {
                    zooKeeper.close();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void testDeleteZookeeper() {
        // final String path, 路径空间
        // int version  版本号  乐观锁机制
        try {
            zooKeeper.delete("/ydlclass", -1);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (zooKeeper != null) {
                try {
                    zooKeeper.close();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Test
    public void testExistsZookeeper() {
        // final String path, 路径空间
        // int version  版本号  乐观锁机制
        try {
            Stat exists = zooKeeper.exists("/ydlclass", null);
            // 修改数据
            zooKeeper.setData("/ydlclass", "hi".getBytes(), -1);

            int version = exists.getVersion();
            System.out.println(version);
            int aversion = exists.getAversion();
            System.out.println(aversion);
            int cversion = exists.getCversion();
            System.out.println(cversion);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (zooKeeper != null) {
                try {
                    zooKeeper.close();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Test
    public void testWatcher() {
        try {
            // 以下三个方法可以注册watcher，可以直接new一个新的watcher，
            // 也可以使用true来选定默认的watcher
            Stat exists = zooKeeper.exists("/ydlclass", true);
            // zooKeeper.getChildren();
            // zooKeeper.getData();
            while (true) {
                Thread.sleep(10000);
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (zooKeeper != null) {
                try {
                    zooKeeper.close();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
