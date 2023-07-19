package com.swift.channelhandler.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

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
        log.debug("服务端接收到的消息 -- {}", byteBuf.toString(CharsetUtil.UTF_8));
        channelHandlerContext.channel().writeAndFlush(Unpooled.copiedBuffer("hello rpc".getBytes(StandardCharsets.UTF_8)));
    }
}
