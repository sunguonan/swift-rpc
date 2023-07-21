package com.swift.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务提供方回复的响应
 *
 * @author sunGuoNan
 * @version 1.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class RpcResponse {
    // 请求的id
    private long requestId;

    // 压缩的类型
    private byte compressType;
    // 序列化的方式
    private byte serializeType;
    // 时间戳
    private long timeStamp;

    // 1 成功，  2 异常
    private byte code;

    // 具体的消息体
    private Object body;
}
