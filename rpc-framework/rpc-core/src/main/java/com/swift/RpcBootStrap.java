package com.swift;

import com.swift.channelhandler.handler.MethodCallHandler;
import com.swift.channelhandler.handler.RpcRequestDecoder;
import com.swift.channelhandler.handler.RpcResponseEncoder;
import com.swift.core.HeartbeatDetector;
import com.swift.discovery.RegisterConfig;
import com.swift.discovery.Registry;
import com.swift.loadbalancer.LoadBalancer;
import com.swift.transport.message.RpcRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 核心的引导程序
 *
 * @author sunGuoNan
 * @version 1.0
 */
@Slf4j
public class RpcBootStrap {
    public static final int PORT = 8090;
    public static final TreeMap<Long, Channel> ANSWER_TIME_CHANNEL_CACHE = new TreeMap<>();

    /**
     * RpcBootStrap是个单例  只希望每个应用程序只有一个实例
     * 单例 --> 懒汉式  私有化构造器  别人不能new
     */
    private static final RpcBootStrap rpcBootStrap = new RpcBootStrap();
    public static String SERIALIZE_TYPE = "jdk";
    public static String COMPRESS_TYPE = "gzip";
    private String appName = "default";
    private RegisterConfig registerConfig;
    private ProtocolConfig protocolConfig;
    // 连接channel缓存 key InetSocketAddress  value Channel
    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);
    // 定义全局挂起的CompletableFuture
    public final static Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(128);
    // 维护暴露的服务列表  key --> interface的全限定名称 value ServiceConfig
    public static final Map<String, ServiceConfig<?>> SERVICE_LIST = new ConcurrentHashMap<>(16);
    // 注册中心
    private Registry registry;
    public static final IdGenerator ID_GENERATOR = new IdGenerator(1, 2);

    public static LoadBalancer LOAD_BALANCER;

    public static ThreadLocal<RpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();

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
     * @param registerConfig 注册中心配置实体
     * @return this 对象实例
     */
    public RpcBootStrap registry(RegisterConfig registerConfig) {
        // 使用registerConfig获取一个配置中心  --- 简单工厂设计模式
        this.registry = registerConfig.getRegister();
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
    public RpcBootStrap publish(ServiceConfig<?> server) {
        // 抽象出注册中心的概念 使用注册中心的实现完成注册
        // zooKeeper = ZookeeperUtil.createZookeeper(); 强耦合
        registry.register(server);

        // 当服务调用方 通过方法名和参数进行方法调用 怎么提供哪一个实现?
        // 1. new一个 2. spring bean工厂  3. 自己维护映射关系  那我们选择自己维护映射关系
        SERVICE_LIST.put(server.getInterface().getName(), server);

        return this;
    }

    /**
     * 批量发布
     *
     * @param server 封装需要发布的服务
     * @return this 对象实例
     */
    public RpcBootStrap publish(List<ServiceConfig<?>> server) {
        server.forEach(this::publish);
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {
        // 创建 boss 和 work
        NioEventLoopGroup boss = new NioEventLoopGroup(2);
        NioEventLoopGroup work = new NioEventLoopGroup(10);
        try {
            // 需要一个服务器引导程序
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap = serverBootstrap.group(boss, work)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    // 日志处理器
                                    .addLast(new LoggingHandler())
                                    // 消息解码器
                                    .addLast(new RpcRequestDecoder())
                                    // 方法调用器
                                    .addLast(new MethodCallHandler())
                                    // 进行响应编码
                                    .addLast(new RpcResponseEncoder());
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(RpcBootStrap.PORT).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                boss.shutdownGracefully().sync();
                work.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 在这个方法中 可以拿到相关配置项  eg 注册中心
     * 那么就可以拿到Reference --> 在将来调用get方法时 生成代理对象
     *
     * @param reference 封装需要发布的服务
     * @return RpcBootStrap 实例
     */
    public RpcBootStrap reference(ReferenceConfig<?> reference) {
        // 开启对服务的心跳检测
        HeartbeatDetector.detectHeartbeat(reference.getInterface().getName());
        // 获取注册中心
        reference.setRegistry(registry);
        return this;
    }

    /**
     * 配置序列化器
     *
     * @param serializeName
     * @return
     */
    public RpcBootStrap serialize(String serializeName) {
        SERIALIZE_TYPE = serializeName;
        return this;
    }

    /**
     * 配置压缩器
     *
     * @param CompressName
     * @return
     */
    public RpcBootStrap compress(String CompressName) {
        COMPRESS_TYPE = CompressName;
        return this;
    }

    public Registry getRegistry() {
        return registry;
    }
}
