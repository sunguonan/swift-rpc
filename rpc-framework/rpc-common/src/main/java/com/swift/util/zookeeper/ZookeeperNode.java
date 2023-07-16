package com.swift.util.zookeeper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author sunGuoNan
 * @version 1.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ZookeeperNode {
    // 结点路径
    private String nodePath;
    // 结点数据
    private byte[] data;
}
