package com.swift.channelhandler.handler;

import com.swift.RpcBootStrap;
import com.swift.ServiceConfig;
import com.swift.enumeration.RequestType;
import com.swift.enumeration.RespCode;
import com.swift.transport.message.RequestPayload;
import com.swift.transport.message.RpcRequest;
import com.swift.transport.message.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

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
        // 1. 获取负载内容
        RequestPayload requestPayload = rpcRequest.getRequestPayload();
        Object result = null;
        if (!(rpcRequest.getRequestType() == RequestType.HEART_BEAT.getId())) {
            // 2. 根据负载内容进行调用
            result = callTargetMethod(requestPayload);
        }
        // 3. 封装响应
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setCode(RespCode.SUCCESS.getCode());
        rpcResponse.setRequestId(rpcRequest.getRequestId());
        rpcResponse.setCompressType(rpcRequest.getCompressType());
        rpcResponse.setSerializeType(rpcRequest.getSerializeType());
        rpcResponse.setTimeStamp(new Date().getTime());
        rpcResponse.setBody(result);
        // 4. 写出响应
        channelHandlerContext.channel().writeAndFlush(rpcResponse);
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
