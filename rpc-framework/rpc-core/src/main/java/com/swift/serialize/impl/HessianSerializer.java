package com.swift.serialize.impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.swift.exception.SerializeException;
import com.swift.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author sunGuoNan
 * @version 1.0
 */
@Slf4j
public class HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {

        if (object == null) {
            return null;
        }

        try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ) {

            Hessian2Output hessian2Output = new Hessian2Output(baos);
            hessian2Output.writeObject(object);
            hessian2Output.flush();
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
        ) {
            Hessian2Input hessian2Input = new Hessian2Input(bais);
            Object object = hessian2Input.readObject();
            log.debug("类【{}】已经完成了反序列化操作.", clazz);
            return (T) object;
        } catch (IOException e) {
            log.error("反序列化对象时发生异常", e);
            throw new SerializeException(e);
        }
    }
}
