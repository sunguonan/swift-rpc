package com.swift.channelhandler.handler;

import com.swift.RpcBootStrap;
import com.swift.ServiceConfig;
import com.swift.enumeration.RequestType;
import com.swift.enumeration.RespCode;
import com.swift.protection.RateLimiter;
import com.swift.protection.TokenBuketRateLimiter;
import com.swift.transport.message.RequestPayload;
import com.swift.transport.message.RpcRequest;
import com.swift.transport.message.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.util.Date;
import java.util.Map;

/**
 * 方法调用处理器
 *
 * @author sunGuoNan
 * @version 1.0
 */
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<RpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        // 1、先封装部分响应
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(rpcRequest.getRequestId());
        rpcResponse.setCompressType(rpcRequest.getCompressType());
        rpcResponse.setSerializeType(rpcRequest.getSerializeType());

        // 2、完成限流相关的操作
        Channel channel = channelHandlerContext.channel();
        SocketAddress socketAddress = channel.remoteAddress();
        Map<SocketAddress, RateLimiter> everyIpRateLimiter =
                RpcBootStrap.getInstance().getConfiguration().getEveryIpRateLimiter();

        RateLimiter rateLimiter = everyIpRateLimiter.get(socketAddress);
        if (rateLimiter == null) {
            rateLimiter = new TokenBuketRateLimiter(10, 10);
            everyIpRateLimiter.put(socketAddress, rateLimiter);
        }
        boolean allowRequest = rateLimiter.allowRequest();

        // 限流
        if (!allowRequest) {
            // 需要封装响应并且返回了
            rpcResponse.setCode(RespCode.RATE_LIMIT.getCode());
        } else if (rpcRequest.getRequestType() == RequestType.HEART_BEAT.getId()) {
            // 需要封装响应并且返回
            rpcResponse.setCode(RespCode.SUCCESS_HEART_BEAT.getCode());
            // 正常调用
        } else {
            // 1、获取负载内容
            RequestPayload requestPayload = rpcRequest.getRequestPayload();

            // 2、根据负载内容进行方法调用
            try {
                Object result = callTargetMethod(requestPayload);
                if (log.isDebugEnabled()) {
                    log.debug("请求【{}】已经在服务端完成方法调用。", rpcRequest.getRequestId());
                }
                // 3、封装响应   我们是否需要考虑另外一个问题，响应码，响应类型
                rpcResponse.setCode(RespCode.SUCCESS.getCode());
                rpcResponse.setBody(result);
            } catch (Exception e){
                log.error("编号为【{}】的请求在调用过程中发生异常。",rpcRequest.getRequestId(),e);
                rpcResponse.setCode(RespCode.FAIL.getCode());
            }
        }

        // 4、写出响应
        channel.writeAndFlush(rpcResponse);
    }

    private Object callTargetMethod(RequestPayload requestPayload) {
        // 1. 获取负载信息
        String interfaceName = requestPayload.getInterfaceName();
        String methodName = requestPayload.getMethodName();
        Class<?>[] parametersType = requestPayload.getParametersType();
        Object[] parametersValue = requestPayload.getParametersValue();

        // 2. 找到暴露出去接口的具体实现
        ServiceConfig<?> serviceConfig = RpcBootStrap.SERVICE_LIST.get(interfaceName);
        Object refImpl = serviceConfig.getRef();

        // 3. 通过反射调用
        Object invokeResult;
        try {
            Class<?> aClass = refImpl.getClass();
            // 3.1 获取方法对象
            Method method = aClass.getMethod(methodName, parametersType);
            // 3.2 执行invoke方法
            invokeResult = method.invoke(refImpl, parametersValue);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            log.error("调用服务【{}】的方法【{}】时发生了异常。", interfaceName, methodName, e);
            throw new RuntimeException(e);
        }

        return invokeResult;
    }
}
