package com.swift;

import com.swift.discovery.RegisterConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * @author sunGuoNan
 * @version 1.0
 */
@Slf4j
public class ConsumerApplication {
    public static void main(String[] args) {
        // 1. 获取代理对象 并且使用ReferenceConfig进行封装
        // reference 类中一定是一个生成代理的模板方法  具体体现就是在调用get方法上
        ReferenceConfig<HelloRpc> reference = new ReferenceConfig<>();
        reference.setInterface(HelloRpc.class);

        // 代理设计模式
        // 1 连接的注册中心
        // 2 从数据中心拉取对应的服务地址
        // 3 选择一个服务并建立连接
        // 4 传递信息  包括 接口名称 方法名 参数列表 获得结果  eg 调用sayHi才是真正执行代理的过程

        // 2. 启动引导程序 核心使用代理设计模式 获取代理对象并传递参数
        RpcBootStrap.getInstance()
                .application("Consumer")
                // 设置注册中心
                .registry(new RegisterConfig("zookeeper://127.0.0.1:2181"))
                .serialize("jdk")
                .compress("gzip")
                // 一但执行这个操作 就会将注册中心的内容传递给reference 
                // reference就可以通过代理做一些事情
                .reference(reference);


        HelloRpc helloRpc = reference.get();
        // 调用get方法获取代理对象
        for (int i = 0; i < 10; i++) {
            String rpc = helloRpc.sayHi("hello rpc");
            log.debug("rpc--->{}", rpc);
        }

    }
}
