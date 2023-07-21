package com.swift.discovery;

import com.swift.channelhandler.ConsumerChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 提供bootstrap单例
 *
 * @author sunGuoNan
 * @version 1.0
 */
@Slf4j
public class NettyBootstrapInitializer {
    private static Bootstrap bootstrap = new Bootstrap();

    static {
        // 创建线程池  EventLoopGroup  
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
        // 添加组
        bootstrap = bootstrap.group(eventExecutors)
                // 添加通道
                .channel(NioSocketChannel.class)
                // 添加处理器
                .handler(new ConsumerChannelInitializer());
    }


    private NettyBootstrapInitializer() {
    }


    public static Bootstrap getBootstrap() {
        return bootstrap;
    }
}
