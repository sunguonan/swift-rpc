package com.swift;

import com.swift.discovery.NettyBootstrapInitializer;
import com.swift.discovery.Registry;
import com.swift.exception.NetworkException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

/**
 * @author sunGuoNan
 * @version 1.0
 */
@Slf4j
public class ReferenceConfig<T> {
    // 需要远程调用的接口的具体实现
    private Class<T> interfaceConsumer;
    // 注册中心
    private Registry registry;

    public ReferenceConfig() {
    }

    public ReferenceConfig(Class<T> interfaceConsumer) {
        this.interfaceConsumer = interfaceConsumer;
    }

    public Class<T> getInterface() {
        return interfaceConsumer;
    }

    public void setInterface(Class<T> interfaceConsumer) {
        this.interfaceConsumer = interfaceConsumer;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    /**
     * 使用代理设计模式 生成一个api接口的代理对象
     *
     * @return 代理对象
     */
    public T get() {
        // 使用动态代理完成事情
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<?>[] interfaces = {interfaceConsumer};
        // 使用动态代理生成代理对象  代理对象 --> 发送请求 处理远程调用的内容
        Object proxy = Proxy.newProxyInstance(classLoader, interfaces, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws InterruptedException {
                // 调用sayHi方法会走入到这个代码段中  method 获取具体方法  args 获取参数列表

                // 1. 从注册中心找到一个可用的服务
                // TODO question 我们每次调用相关方法的时候都需要去注册中心拉取服务列表吗  本地缓存+watch
                // TODO          我们如何选择一个可用的服务 而不是只获取第一个  轮询 随机 。。。 负载均衡策略
                InetSocketAddress inetSocketAddress =
                        registry.lookup(interfaceConsumer.getName());

                log.debug("服务调用方发现了可用主机{}", inetSocketAddress);
                // 2. 使用netty连接服务器 发送服务的名字 方法名字 参数列表  得到最后的结果
                // 把整个连接过程放在这里不是很合适 这样也就是说明每次调用都会产生一个新的连接netty 
                // 解决方法 缓存channel 从缓存中拿到channel 如果拿不到 创建新连接并缓存channel

                // 2.1 从全局缓存中拿到一个channel
                Channel channel = RpcBootStrap.CHANNEL_CACHE.get(inetSocketAddress);

                if (channel == null) {
                    // 创建新连接 并缓存channel
                    channel = NettyBootstrapInitializer.getBootstrap()
                            .connect(inetSocketAddress).await().channel();
                    RpcBootStrap.CHANNEL_CACHE.put(inetSocketAddress, channel);
                }

                if (channel == null) {
                    throw new NetworkException("获取通道发生异常");
                }

                ChannelFuture channelFuture = channel.writeAndFlush(null);

                return null;
            }
        });
        return (T) proxy;
    }
}
