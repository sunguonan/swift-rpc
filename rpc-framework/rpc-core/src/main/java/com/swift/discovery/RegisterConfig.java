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
        String registerType = getRegisterType(connectString, true).toLowerCase().trim();
        if (registerType.equals("zookeeper")) {
            String registerHost = getRegisterType(connectString, false);
            return new ZookeeperRegister(registerHost, Constant.DEFAULT_ZK_Session_Timeout);
        }
        throw new DiscoveryException("未发现合适的注册中心");
    }

    /**
     * 获取连接url的类型和端口号
     *
     * @param connectString
     * @param ifType
     * @return
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
