package com.swift.channelhandler.handler;

import com.swift.RpcBootStrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * 这是一个用来测试netty接收消息的类
 *
 * @author sunGuoNan
 * @version 1.0
 */
@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {

    /**
     * 处理测试消息的handler
     *
     * @param channelHandlerContext channelHandlerContext
     * @param byteBuf               byteBuf
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
        String result = byteBuf.toString(CharsetUtil.UTF_8);
        log.debug("客户端接收到的消息 -- {}", result);
        // 接收到消息 存放到completableFuture中
        CompletableFuture<Object> completableFuture = RpcBootStrap.PENDING_REQUEST.get(1L);
        completableFuture.complete(result);
    }
}
