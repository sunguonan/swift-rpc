package com.swift;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

/**
 * @author sunGuoNan
 * @version 1.0
 */
public class NettyTest {


    @Test
    public void testCompositeByteBuf() {
        // 模拟请求头和请求体
        ByteBuf header = Unpooled.buffer();
        ByteBuf body = Unpooled.buffer();

        // 通过逻辑组装并不是物理拷贝实现在jvm这层的零拷贝
        CompositeByteBuf httpBuf = Unpooled.compositeBuffer();
        httpBuf.addComponents(header, body);
    }

    @Test
    public void testWrap() {
        byte[] bytes = new byte[1024];
        byte[] bytes1 = new byte[1024];
        // 共享数组里面的内容 这也不是拷贝
        ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes, bytes1);
    }


    @Test
    public void testSlice() {
        ByteBuf byteBuf = Unpooled.buffer();
        ByteBuf header = byteBuf.slice(0, 5);
        ByteBuf body = byteBuf.slice(5, 10);
    }


}
