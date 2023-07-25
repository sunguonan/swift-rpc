package com.swift.watch;


import com.swift.RpcBootStrap;
import com.swift.discovery.NettyBootstrapInitializer;
import com.swift.discovery.Registry;
import com.swift.loadbalancer.LoadBalancer;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * 检测到服务上下线
 * 当zk中的服务节点发送改变 就重新拉取服务列表
 *
 * @author sunGuoNan
 * @version 1.0
 */
@Slf4j
public class UpAndDownWatcher implements Watcher {
    @Override
    public void process(WatchedEvent event) {

        // 当前的阶段是否发生了变化
        if (event.getType() == Event.EventType.NodeChildrenChanged) {
            if (log.isDebugEnabled()) {
                log.debug("检测到服务【{}】下有节点上/下线，将重新拉取服务列表...", event.getPath());
            }
            String serviceName = getServiceName(event.getPath());
            // 
            Registry registry = RpcBootStrap.getInstance().getConfiguration().getRegistryConfig().getRegister();
            List<InetSocketAddress> addresses = registry.lookup(serviceName);

            // 处理新增的节点
            for (InetSocketAddress address : addresses) {
                // 新增的节点   会在address 不在CHANNEL_CACHE
                // 下线的节点   可能会在CHANNEL_CACHE 不在address
                if (!RpcBootStrap.CHANNEL_CACHE.containsKey(address)) {
                    // 根据地址建立连接，并且缓存
                    Channel channel = null;
                    try {
                        channel = NettyBootstrapInitializer.getBootstrap()
                                .connect(address).sync().channel();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    RpcBootStrap.CHANNEL_CACHE.put(address, channel);
                }
            }

            // 处理下线的节点 可能会在CHANNEL_CACHE 不在address
            for (Map.Entry<InetSocketAddress, Channel> entry : RpcBootStrap.CHANNEL_CACHE.entrySet()) {
                if (!addresses.contains(entry.getKey())) {
                    RpcBootStrap.CHANNEL_CACHE.remove(entry.getKey());
                }
            }

            // 获得负载均衡器，进行重新的loadBalance
            LoadBalancer loadBalancer = RpcBootStrap.getInstance().getConfiguration().getLoadBalancer();
            loadBalancer.reLoadBalance(serviceName, addresses);
        }
    }

    private String getServiceName(String path) {
        String[] split = path.split("/");
        return split[split.length - 1];
    }
}
