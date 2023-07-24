package com.swift.channelhandler.handler;

import com.swift.RpcBootStrap;
import com.swift.transport.message.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * 这是一个用来测试netty接收消息的类
 *
 * @author sunGuoNan
 * @version 1.0
 */
@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<RpcResponse> {

    /**
     * 处理测试消息的handler
     *
     * @param channelHandlerContext channelHandlerContext
     * @param rpcResponse           byteBuf
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) {
        // 服务提供方返回的结果
        Object result = rpcResponse.getBody();
        // 接收到消息 存放到completableFuture中
        CompletableFuture<Object> completableFuture = RpcBootStrap.PENDING_REQUEST.get(rpcResponse.getRequestId());
        completableFuture.complete(result);
        log.debug("以寻找到编号为【{}】的completableFuture，处理响应结果。", rpcResponse.getRequestId());
    }
}
