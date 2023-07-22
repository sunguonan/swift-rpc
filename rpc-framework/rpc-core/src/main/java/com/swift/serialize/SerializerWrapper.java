package com.swift.serialize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author sunGuoNan
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SerializerWrapper {
    // 序列化类型编码
    private byte code;
    // 序列化类型
    private String type;
    // 具体实现
    private Serializer serializer;
}
