package com.swift;

import com.swift.annotation.RpcApi;
import com.swift.channelhandler.handler.MethodCallHandler;
import com.swift.channelhandler.handler.RpcRequestDecoder;
import com.swift.channelhandler.handler.RpcResponseEncoder;
import com.swift.config.Configuration;
import com.swift.core.HeartbeatDetector;
import com.swift.discovery.RegisterConfig;
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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 核心的引导程序
 *
 * @author sunGuoNan
 * @version 1.0
 */
@Slf4j
public class RpcBootStrap {
    public static final TreeMap<Long, Channel> ANSWER_TIME_CHANNEL_CACHE = new TreeMap<>();

    /**
     * RpcBootStrap是个单例  只希望每个应用程序只有一个实例
     * 单例 --> 懒汉式  私有化构造器  别人不能new
     */
    private static final RpcBootStrap rpcBootStrap = new RpcBootStrap();
    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);
    // 定义全局挂起的CompletableFuture
    public final static Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(128);
    // 维护暴露的服务列表  key --> interface的全限定名称 value ServiceConfig
    public static final Map<String, ServiceConfig<?>> SERVICE_LIST = new ConcurrentHashMap<>(16);


    public static ThreadLocal<RpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();
    private Configuration configuration;

    private RpcBootStrap() {
        // 私有化构造器  做一些初始化的事情
        configuration = new Configuration();
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
        configuration.setAppName(appName);
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
        configuration.setRegistryConfig(registerConfig);
        return this;
    }

    /**
     * 负载均衡策略
     *
     * @param loadBalancer
     * @return
     */
    public RpcBootStrap loadBalancer(LoadBalancer loadBalancer) {
        configuration.setLoadBalancer(loadBalancer);
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
        configuration.getRegistryConfig().getRegister().register(server);

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
            ChannelFuture channelFuture = serverBootstrap.bind(configuration.getPort()).sync();
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
        reference.setRegistry(configuration.getRegistryConfig().getRegister());
        return this;
    }

    /**
     * 配置序列化器
     *
     * @param serializeType 设置序列化类型
     * @return
     */
    public RpcBootStrap serialize(String serializeType) {
        configuration.setSerializeType(serializeType);
        return this;
    }

    /**
     * 配置压缩器
     *
     * @param CompressType 压缩类型
     * @return
     */
    public RpcBootStrap compress(String CompressType) {
        configuration.setCompressType(CompressType);
        return this;
    }

    /**
     * 扫描包，进行批量注册
     *
     * @param packageName 包名
     * @return this本身
     */
    public RpcBootStrap scan(String packageName) {
        // 1、需要通过packageName获取其下的所有的类的权限定名称
        List<String> classNames = getAllClassNames(packageName);

        // 2、通过反射获取他的接口，构建具体实现
        List<Class<?>> classes = classNames.stream()
                .map(className -> {
                    try {
                        return Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    // 检查类是否被标记了RpcApi注解。
                }).filter(clazz -> clazz.getAnnotation(RpcApi.class) != null)
                .collect(Collectors.toList());

        for (Class<?> clazz : classes) {
            // 获取他的接口
            Class<?>[] interfaces = clazz.getInterfaces();
            Object instance = null;
            try {
                instance = clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }


            for (Class<?> anInterface : interfaces) {
                ServiceConfig<?> serviceConfig = new ServiceConfig<>();
                serviceConfig.setInterface(anInterface);
                serviceConfig.setRef(instance);
                if (log.isDebugEnabled()) {
                    log.debug("---->已经通过包扫描，将服务【{}】发布.", anInterface);
                }
                // 3、发布
                publish(serviceConfig);
            }

        }
        return this;
    }

    private List<String> getAllClassNames(String packageName) {
        // 1、通过packageName获得绝对路径
        // com.swift.xxx.yyy -> E://xxx/xww/sss/com/swift/xxx/yyy
        String basePath = packageName.replaceAll("\\.", "/");
        URL url = ClassLoader.getSystemClassLoader().getResource(basePath);
        if (url == null) {
            throw new RuntimeException("包扫描时，发现路径不存在.");
        }
        // 获取绝对路径
        String absolutePath = url.getPath();

        // 递归获取文件 ---> 获取全部的class文件
        List<String> classNames = new ArrayList<>();
        classNames = recursionFile(absolutePath, classNames, basePath);
        return classNames;
    }

    private List<String> recursionFile(String absolutePath, List<String> classNames, String basePath) {
        // 获取文件
        File file = new File(absolutePath);
        // 判断文件是否是文件夹
        if (file.isDirectory()) {
            // 找到文件夹的所有的文件   （包含com/swift/下的 文件夹和class文件）
            File[] children = file.listFiles(pathname -> pathname.isDirectory() || pathname.getPath().contains(".class"));
            if (children == null || children.length == 0) {
                return classNames;
            }
            // 遍历根节点下的子结点
            for (File child : children) {
                if (child.isDirectory()) {
                    // 递归调用
                    recursionFile(child.getAbsolutePath(), classNames, basePath);
                } else {
                    // 文件 --> 类的权限定名称
                    String className = getClassNameByAbsolutePath(child.getAbsolutePath(), basePath);
                    classNames.add(className);
                }
            }

        } else {
            // 文件 --> 类的权限定名称
            String className = getClassNameByAbsolutePath(absolutePath, basePath);
            classNames.add(className);
        }
        return classNames;
    }

    private String getClassNameByAbsolutePath(String absolutePath, String basePath) {
        // D:\workBench\sourceCode\swift-rpc\rpc-framework\rpc-core\target\classes\com\swift\channelhandler\ConsumerChannelInitializer.class
        // com\swift\channelhandler\ConsumerChannelInitializer.class --> com.swift.channelhandler.ConsumerChannelInitializer
        String fileName = absolutePath
                .substring(absolutePath.indexOf(basePath.replaceAll("/", "\\\\")))
                .replaceAll("\\\\", ".");

        fileName = fileName.substring(0, fileName.indexOf(".class"));
        return fileName;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
