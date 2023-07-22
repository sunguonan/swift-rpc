package com.swift.serialize.impl;

import com.swift.exception.SerializeException;
import com.swift.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @author sunGuoNan
 * @version 1.0
 */
@Slf4j
public class JdkSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {

        if (object == null) {
            return null;
        }

        try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream outputStream = new ObjectOutputStream(baos)
        ) {
            outputStream.writeObject(object);
            byte[] result = baos.toByteArray();
            log.debug("对象【{}】已经完成了序列化操作，序列化后的字节数为【{}】", object, result.length);
            return result;
        } catch (IOException e) {
            log.error("序列化对象时发生异常", e);
            throw new SerializeException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || clazz == null) {
            return null;
        }
        try (
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                ObjectInputStream inputStream = new ObjectInputStream(bais)
        ) {
            Object object = inputStream.readObject();
            log.debug("类【{}】已经完成了反序列化操作.", clazz);
            return (T) object;
        } catch (IOException | ClassNotFoundException e) {
            log.error("反序列化对象时发生异常", e);
            throw new SerializeException(e);
        }
    }
}
