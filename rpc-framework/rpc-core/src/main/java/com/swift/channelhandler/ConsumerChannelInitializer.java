package com.swift.channelhandler;

import com.swift.channelhandler.handler.MySimpleChannelInboundHandler;
import com.swift.channelhandler.handler.RpcMessageEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 通道的初始化器 在初始化器中可以添加很多个处理器handler 执行操作
 *
 * @author sunGuoNan
 * @version 1.0
 */
public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    /**
     * 初始化通道
     *
     * @param socketChannel 套接字管道
     */
    @Override
    protected void initChannel(SocketChannel socketChannel) {
        socketChannel.pipeline()
                // 添加netty自带的log处理器
                .addLast(new LoggingHandler(LogLevel.DEBUG))
                // 消息编码器
                .addLast(new RpcMessageEncoder())
                // 自定义消息接收处理器
                .addLast(new MySimpleChannelInboundHandler());
    }
}
