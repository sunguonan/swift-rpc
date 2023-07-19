package com.swift.transport.message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * 消息转化器  封装报文 将需要发送的消息进行转化 转化为二进制数组
 *
 * @author sunGuoNan
 * @version 1.0
 */
@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcRequest> {
    /**
     * 将需要发送的消息进行编码 转化为二进制数组
     *
     * @param channelHandlerContext
     * @param rpcRequest
     * @param byteBuf
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest, ByteBuf byteBuf) throws Exception {
        // 5个字节的魔术值
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        // 1个字节的版本号
        byteBuf.writeByte(MessageFormatConstant.VERSION);
        // 2个字节的头部长度
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
        // 4个字节的总占用长度   full length
        byteBuf.writeShort(byteBuf.writerIndex() + 4);
        // 3个类型占用的字节数
        byteBuf.writeByte(rpcRequest.getRequestType());
        byteBuf.writeByte(rpcRequest.getSerializeType());
        byteBuf.writeByte(rpcRequest.getCompressType());
        // 8个字节的请求id
        byteBuf.writeLong(rpcRequest.getRequestId());
        byte[] body = getBodyBytes(rpcRequest.getRequestPayload());
        byteBuf.writeBytes(body);

        // 保存当前写指针的位置
        int writerIndex = byteBuf.writerIndex();
        // 将写指针移动到写入(总长度的位置) 
        byteBuf.writerIndex(8);
        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + body.length);
        // 写指针归位
        byteBuf.writerIndex(writerIndex);

    }

    /**
     * 将对象序列化为字节数组
     *
     * @param requestPayload 对象实体
     * @return 字节数组
     */
    private byte[] getBodyBytes(RequestPayload requestPayload) {
        // TODO  针对不同的消息类型需要做不同的处理 心跳检测,没有payload
        // 进行序列化
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(requestPayload);
            // TODO 压缩
            return bos.toByteArray();
        } catch (IOException e) {
            log.debug("转换对象为字节数组失败", e);
            throw new RuntimeException(e);
        }

    }
}
