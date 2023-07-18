package com.swift.discovery;

import com.swift.Constant;
import com.swift.discovery.impl.ZookeeperRegister;
import com.swift.exception.DiscoveryException;

/**
 * 封装需要发布的服务
 *
 * @author sunGuoNan
 * @version 1.0
 */
public class RegisterConfig {
    // 连接名称
    // 定义可连接的url  zookeeper://127.0.0.1:2181  Redis://192.168.201.128:3306
    private String connectString;

    public RegisterConfig() {
    }

    public RegisterConfig(String connectString) {
        this.connectString = connectString;
    }

    /**
     * 简单工厂模式
     * 从中获取一个匹配的注册中心
     *
     * @return Registry 具体的数据中心实例
     */
    public Registry getRegister() {
        // 获取连接的类型
        String registerType = getRegisterType(connectString, true).toLowerCase().trim();
        // 判断连接类型 并建立联机
        if (registerType.equals("zookeeper")) {
            String registerHost = getRegisterType(connectString, false);
            return new ZookeeperRegister(registerHost, Constant.DEFAULT_ZK_Session_Timeout);
        }
        throw new DiscoveryException("未发现合适的注册中心");
    }

    /**
     * 获取连接url的类型和端口号
     *
     * @param connectString 连接的url
     * @param ifType        是否获取连接类型
     * @return typeAndHost[0] 连接类型  | typeAndHost[1] 连接的host
     */
    public String getRegisterType(String connectString, boolean ifType) {
        String[] typeAndHost = connectString.split("://");
        if (typeAndHost.length != 2) {
            throw new RuntimeException("注册中心连接url不合法");
        }
        if (ifType) {
            return typeAndHost[0];
        } else {
            return typeAndHost[1];
        }
    }


}
