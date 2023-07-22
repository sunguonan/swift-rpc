package com.swift.serialize.impl;

import com.swift.serialize.Serializer;

/**
 * @author sunGuoNan
 * @version 1.0
 */
public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        return new byte[0];
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return null;
    }
}
