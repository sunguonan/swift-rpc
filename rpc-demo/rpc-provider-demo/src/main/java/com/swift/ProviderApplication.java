package com.swift;


import com.swift.discovery.RegisterConfig;
import com.swift.impl.HelloRpcImpl;

/**
 * @author sunGuoNan
 * @version 1.0
 */

public class ProviderApplication {
    public static void main(String[] args) {
        // 服务提供方 --> 需要注册服务 启动服务
        // 1. 封装需要发布的服务  把接口和具体实现封装进去
        ServiceConfig<HelloRpc> server = new ServiceConfig<>();
        // 设置需要提供服务的接口
        server.setInterface(HelloRpc.class);
        // 设置具体被调接口的具体实现
        server.setRef(new HelloRpcImpl());

        // 2. 启动引导程序  启动服务
        // 2.1 配置 应用的名称、注册中心、序列化协议、压缩协议。。。。
        // 2.2 发布服务
        // 2.3 启动服务
        RpcBootStrap.getInstance()
                .application("Provider")
                .registry(new RegisterConfig("zookeeper://127.0.0.1:2181"))
                .serialize("jdk")
                // 发布服务  ---> 将对应的接口和实现注册到服务中心
                // .publish(server)
                // 扫包
                .scan("com.swift")
                // 启动服务
                .start();
    }
}
