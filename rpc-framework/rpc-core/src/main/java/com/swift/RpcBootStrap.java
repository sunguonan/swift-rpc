package com.swift;

import com.swift.util.NetUtils;
import com.swift.util.zookeeper.ZookeeperNode;
import com.swift.util.zookeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

/**
 * 核心的引导程序
 *
 * @author sunGuoNan
 * @version 1.0
 */
@Slf4j
public class RpcBootStrap {
    /**
     * RpcBootStrap是个单例  只希望每个应用程序只有一个实例
     * 单例 --> 懒汉式  私有化构造器  别人不能new
     */
    private static final RpcBootStrap rpcBootStrap = new RpcBootStrap();

    private String appName = "default";
    private RegisterConfig registerConfig;

    private ProtocolConfig protocolConfig;

    private ZooKeeper zooKeeper;
    private int port = 8080;

    private RpcBootStrap() {
        // 私有化构造器  做一些初始化的事情
    }

    /**
     * 获取实例对象
     *
     * @return this 对象实例
     */
    public static RpcBootStrap getInstance() {
        return rpcBootStrap;
    }

    /**
     * 配置应用的名称
     *
     * @param appName 应用名称
     * @return this 对象实例
     */
    public RpcBootStrap application(String appName) {
        this.appName = appName;
        return this;
    }

    /**
     * 配置注册中心
     *
     * @param registerConfig 注册中心
     * @return this 对象实例
     */
    public RpcBootStrap registry(RegisterConfig registerConfig) {
        // 这里维护了一个zookeeper的实例 但是会与当前工程耦合到一起
        // 后面会进行修改 修改成可以兼容不同的服务
        zooKeeper = ZookeeperUtil.createZookeeper();
        this.registerConfig = registerConfig;
        return this;
    }

    /**
     * 配置序列化协议
     *
     * @param protocolConfig 序列化协议  eg 默认jdk
     * @return this 对象实例
     */
    public RpcBootStrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        return this;
    }

    /**
     * 配置发布服务
     * 将需要发布服务的接口以及对应的实现 注册到服务中心
     * 单个发布
     *
     * @param server 封装需要发布的服务
     * @return this 对象实例
     */
    public RpcBootStrap publish(ServerConfig<?> server) {

        // 创建服务节点
        String parentNode = Constant.BASE_PROVIDER_PATH + "/" + server.getInterface().getName();
        ZookeeperNode zookeeperParentNode = new ZookeeperNode(parentNode, null);
        // 发布结点  这个结点是一个持久的结点
        if (!ZookeeperUtil.exists(zooKeeper, parentNode, null)) {
            ZookeeperUtil.createNode(zooKeeper, zookeeperParentNode, null, CreateMode.PERSISTENT);
        }

        // 创建本机结点  以ip:port的形式  该结点是临时结点
        // 服务提供的端口一般由自己设置
        // ip一般是局域网地址  不是127.0.0.1
        String localNode = parentNode + "/" + NetUtils.getIp() + ":" + port;
        ZookeeperNode zookeeperLocalNode = new ZookeeperNode(localNode, null);
        // 发布结点  这个节点是一个临时节点
        if (!ZookeeperUtil.exists(zooKeeper, localNode, null)) {
            ZookeeperUtil.createNode(zooKeeper, zookeeperLocalNode, null, CreateMode.EPHEMERAL);
        }
        return this;
    }

    /**
     * 批量发布
     *
     * @param server 封装需要发布的服务
     * @return this 对象实例
     */
    public RpcBootStrap publish(List<ServerConfig<?>> server) {
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 在这个方法中 可以拿到相关配置项  eg 注册中心
     * 那么就可以拿到Reference --> 在将来调用get方法时 生成代理对象
     *
     * @param reference 封装需要发布的服务
     */
    public void reference(ReferenceConfig<?> reference) {

    }
}
