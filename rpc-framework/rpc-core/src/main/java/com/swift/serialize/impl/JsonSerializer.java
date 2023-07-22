package com.swift.serialize.impl;

import com.alibaba.fastjson2.JSON;
import com.swift.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

/**
 * 使用JSON作为序列化  会出现一定的问题 所以我们只做出实现 并不关心具体效果
 *
 * @author sunGuoNan
 * @version 1.0
 */
@Slf4j
public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {

        if (object == null) {
            return null;
        }
        byte[] result = JSON.toJSONBytes(object);
        log.debug("对象【{}】已经完成了序列化操作，序列化后的字节数为【{}】", object, result.length);
        return result;
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || clazz == null) {
            return null;
        }
        Object object = JSON.parseObject(bytes, clazz);
        log.debug("类【{}】已经完成了反序列化操作.", clazz);
        return (T) object;
    }
}
