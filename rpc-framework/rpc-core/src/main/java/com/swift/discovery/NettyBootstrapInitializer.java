package com.swift.discovery;

import com.swift.RpcBootStrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

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
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
                                String result = byteBuf.toString(CharsetUtil.UTF_8);
                                log.debug("客户端接收到的消息 -- {}", result);
                                // 接收到消息 存放到completableFuture中
                                CompletableFuture<Object> completableFuture = RpcBootStrap.PENDING_REQUEST.get(1L);
                                completableFuture.complete(result);
                            }
                        });
                    }
                });
    }


    private NettyBootstrapInitializer() {
    }


    public static Bootstrap getBootstrap() {
        return bootstrap;
    }
}
