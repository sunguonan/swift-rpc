package com.swift.config;


import com.swift.IdGenerator;
import com.swift.ProtocolConfig;
import com.swift.compress.Compressor;
import com.swift.compress.impl.GzipCompressor;
import com.swift.discovery.RegisterConfig;
import com.swift.loadbalancer.LoadBalancer;
import com.swift.loadbalancer.impl.RoundRobinLoadBalancer;
import com.swift.protection.CircuitBreaker;
import com.swift.protection.RateLimiter;
import com.swift.protection.TokenBuketRateLimiter;
import com.swift.serialize.Serializer;
import com.swift.serialize.impl.JdkSerializer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局的配置类，代码配置-->xml配置-->默认项
 *
 * @author sunGuoNan
 * @version 1.0
 */
@Data
@Slf4j
public class Configuration {
    // 配置信息-->id发射器
    public IdGenerator idGenerator = new IdGenerator(1, 2);
    // 配置信息-->端口号
    private int port = 8094;
    // 配置信息-->应用程序的名字
    private String appName = "default";
    // 配置信息-->注册中心
    private RegisterConfig registryConfig = new RegisterConfig("zookeeper://127.0.0.1:2181"); 
    // 配置信息-->序列化协议
    private String serializeType = "jdk";
    // 配置信息-->压缩使用的协议
    private String compressType = "gzip";
    // 配置信息-->负载均衡策略
    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();
    // 分组信息
    private String group = "default";

    // 为每一个ip配置一个限流器
    private final Map<SocketAddress, RateLimiter> everyIpRateLimiter = new ConcurrentHashMap<>(16);
    // 为每一个ip配置一个断路器，熔断
    private final Map<SocketAddress, CircuitBreaker> everyIpCircuitBreaker = new ConcurrentHashMap<>(16);

    // 读xml，dom4j
    public Configuration() {

        // spi机制发现相关配置项
        SpiResolver spiResolver = new SpiResolver();
        spiResolver.loadFromSpi(this);

        // 读取xml获得上边的信息
        XmlResolver xmlResolver = new XmlResolver();
        xmlResolver.loadFromXml(this);
    }
}
