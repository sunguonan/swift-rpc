package com.swift;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * @author sunGuoNan
 * @version 1.0
 */
public class AppClient {


    public static void main(String[] args) {

        new AppClient().run();
    }

    public void run() {

        // 创建线程池  EventLoopGroup  
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();

        //  客户端需要创建一个辅助类
        Bootstrap bootstrap = new Bootstrap();
        // 添加组
        bootstrap = bootstrap.group(eventExecutors)
                // 添加通道
                .channel(NioSocketChannel.class)
                // 绑定端口
                .remoteAddress(new InetSocketAddress(8080))
                // 添加处理器
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new ClientChannelHandler());
                    }
                });


        // 尝试连接服务器
        ChannelFuture channelFuture = null;
        try {
            channelFuture = bootstrap.connect().sync();
            // 使用channel写出数据  写出的数据需要用处理器处理相对应的内容
            channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer("hello netty".getBytes(StandardCharsets.UTF_8)));
            // 等待服务器的消息  并且关闭连接
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


}
