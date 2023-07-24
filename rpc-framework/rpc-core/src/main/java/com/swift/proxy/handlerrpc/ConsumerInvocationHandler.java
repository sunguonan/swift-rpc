package com.swift.proxy.handlerrpc;

import com.swift.RpcBootStrap;
import com.swift.compress.CompressorFactory;
import com.swift.discovery.NettyBootstrapInitializer;
import com.swift.discovery.Registry;
import com.swift.enumeration.RequestType;
import com.swift.exception.NetworkException;
import com.swift.serialize.SerializerFactory;
import com.swift.transport.message.RequestPayload;
import com.swift.transport.message.RpcRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 封装了客户端通信的基础逻辑，每一个代理对象的远程调用过程都封装在了invoke方法中
 * 1、 发现可用服务    2、建立连接   3、发送请求   4、得到结果
 *
 * @author sunGuoNan
 * @version 1.0
 */
@Slf4j
public class ConsumerInvocationHandler implements InvocationHandler {

    // 注册中心
    private final Registry registry;
    // 接口
    private final Class<?> interfaceConsumer;

    public ConsumerInvocationHandler(Registry registry, Class<?> interfaceConsumer) {
        this.registry = registry;
        this.interfaceConsumer = interfaceConsumer;
    }

    /**
     * 调用sayHi方法会走入到这个代码段中  method 获取具体方法  args 获取参数列表
     *
     * @return 从服务提供者中获取返回参数
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {

        // 1. 封装报文 封装请求
        RequestPayload requestPayload = RequestPayload.builder()
                .interfaceName(interfaceConsumer.getName())
                .methodName(method.getName())
                .parametersType(method.getParameterTypes())
                .parametersValue(args)
                .returnType(method.getReturnType()).build();

        RpcRequest rpcRequest = RpcRequest.builder()
                .requestId(RpcBootStrap.ID_GENERATOR.getId())
                .requestType(RequestType.REQUEST.getId())
                .compressType(CompressorFactory.getCompressor(RpcBootStrap.COMPRESS_TYPE).getCode())
                .serializeType(SerializerFactory.getSerializer(RpcBootStrap.SERIALIZE_TYPE).getCode())
                .timeStamp(new Date().getTime())
                .requestPayload(requestPayload).build();

        // 创建本地线程 threadLocal
        RpcBootStrap.REQUEST_THREAD_LOCAL.set(rpcRequest);

        // 2. 使用负载均衡策略获取主机
        InetSocketAddress inetSocketAddress = RpcBootStrap.LOAD_BALANCER.selectServiceAddress(interfaceConsumer.getName());
        log.debug("服务调用方发现了可用主机{}", inetSocketAddress);

        // 3. 获取一个可用通道
        Channel channel = getAvailableChannel(inetSocketAddress);
        log.debug("服务调用方获取一个可用通道{}", channel);


        // 4. 写出报文
        // 将objectFuture暴露出去 方便让接收的pipeline处理对应的消息 并保存到completableFuture中
        CompletableFuture<Object> objectFuture = new CompletableFuture<>();
        // 挂起objectFuture 暴露CompletableFuture
        RpcBootStrap.PENDING_REQUEST.put(rpcRequest.getRequestId(), objectFuture);
        // 写出数据 这个请求的实例会进入pipeline 会执行出栈等一系列操作 第一个进入处理器的一定是将对象转化为二进制数据
        channel.writeAndFlush(rpcRequest)
                .addListener((ChannelFutureListener) promise -> {
                    // 异步任务不能完成的情况
                    if (!promise.isSuccess()) {
                        log.debug("异步任务完成失败", promise.cause());
                        // 处理异常
                        objectFuture.completeExceptionally(promise.cause());
                    }
                });

        // 清理threadLocal
        RpcBootStrap.REQUEST_THREAD_LOCAL.remove();


        // 5. 获得响应
        // 返回结果是 服务提供者返回的最后结果 也就是从接收的pipeline的completableFuture中获取结果
        try {
            return objectFuture.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.debug("获取响应结果失败", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取一个可用的通道列表
     *
     * @param inetSocketAddress 获取IP地址和端口号
     * @return channel通道连接
     */
    private Channel getAvailableChannel(InetSocketAddress inetSocketAddress) {
        // 1. 从全局缓存中拿到一个channel
        Channel channel = RpcBootStrap.CHANNEL_CACHE.get(inetSocketAddress);

        // 2. 如果获取不到channel 就去建立连接 并缓存channel
        if (channel == null) {
            CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
            // 使用异步策略
            NettyBootstrapInitializer.getBootstrap().connect(inetSocketAddress)
                    .addListener((ChannelFutureListener) promise -> {
                        // 判断是否执行完成
                        if (promise.isDone()) {
                            log.debug("异步任务执行完成");
                            // 异步的获取channel 我们已经完成
                            channelFuture.complete(promise.channel());
                        } else if (!promise.isSuccess()) {
                            log.debug("异步任务完成失败", promise.cause());
                            // 处理异常
                            channelFuture.completeExceptionally(promise.cause());
                        }
                    });

            // 3. 阻塞获取channel 这也是一个同步操作  
            try {
                channel = channelFuture.get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.debug("获取通道时发生异常", e);
                throw new RuntimeException(e);
            }

            // 4. 缓存channel
            RpcBootStrap.CHANNEL_CACHE.put(inetSocketAddress, channel);
        }

        if (channel == null) {
            log.error("获取通道发生异常{}", channel);
            throw new NetworkException("获取通道发生异常");
        }

        return channel;
    }
}
