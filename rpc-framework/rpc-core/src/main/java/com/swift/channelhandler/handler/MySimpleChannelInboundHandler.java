package com.swift.channelhandler.handler;

import com.swift.RpcBootStrap;
import com.swift.enumeration.RespCode;
import com.swift.exception.ResponseException;
import com.swift.protection.CircuitBreaker;
import com.swift.transport.message.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Map;
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
        CompletableFuture<Object> completableFuture = RpcBootStrap.PENDING_REQUEST.get(rpcResponse.getRequestId());

        SocketAddress socketAddress = channelHandlerContext.channel().remoteAddress();
        Map<SocketAddress, CircuitBreaker> everyIpCircuitBreaker = RpcBootStrap.getInstance().getConfiguration().getEveryIpCircuitBreaker();
        CircuitBreaker circuitBreaker = everyIpCircuitBreaker.get(socketAddress);

        byte code = rpcResponse.getCode();
        if (code == RespCode.FAIL.getCode()) {
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("当前id为[{}]的请求，返回错误的结果，响应码[{}].", rpcResponse.getRequestId(), rpcResponse.getCode());
            throw new ResponseException(code, RespCode.FAIL.getDesc());
        } else if (code == RespCode.RATE_LIMIT.getCode()) {
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("当前id为[{}]的请求，被限流，响应码[{}].", rpcResponse.getRequestId(), rpcResponse.getCode());
            throw new ResponseException(code, RespCode.RATE_LIMIT.getDesc());

        } else if (code == RespCode.RESOURCE_NOT_FOUND.getCode()) {
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("当前id为[{}]的请求，未找到目标资源，响应码[{}].", rpcResponse.getRequestId(), rpcResponse.getCode());
            throw new ResponseException(code, RespCode.RESOURCE_NOT_FOUND.getDesc());
        } else if (code == RespCode.SUCCESS.getCode()) {
            // 服务提供方，给予的结果
            Object returnValue = rpcResponse.getBody();
            completableFuture.complete(returnValue);
            if (log.isDebugEnabled()) {
                log.debug("以寻找到编号为【{}】的completableFuture，处理响应结果。", rpcResponse.getRequestId());
            }
        } else if (code == RespCode.SUCCESS_HEART_BEAT.getCode()) {
            completableFuture.complete(null);
            if (log.isDebugEnabled()) {
                log.debug("以寻找到编号为【{}】的completableFuture,处理心跳检测，处理响应结果。", rpcResponse.getRequestId());
            }
        }
    }
}
