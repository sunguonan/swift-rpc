package com.swift.channelhandler.handler;

import com.swift.transport.message.MessageFormatConstant;
import com.swift.transport.message.RequestPayload;
import com.swift.transport.message.RpcRequest;
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
        // 2个字节的头部的长度
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
        // 总长度不清楚，不知道body的长度 writeIndex(写指针)
        byteBuf.writerIndex(byteBuf.writerIndex() + MessageFormatConstant.FULL_FIELD_LENGTH);
        // 3个类型
        byteBuf.writeByte(rpcRequest.getRequestType());
        byteBuf.writeByte(rpcRequest.getSerializeType());
        byteBuf.writeByte(rpcRequest.getCompressType());
        // 8字节的请求id
        byteBuf.writeLong(rpcRequest.getRequestId());

        // 写入请求体（requestPayload）
        byte[] body = getBodyBytes(rpcRequest.getRequestPayload());
        if (body != null) {
            byteBuf.writeBytes(body);
        }
        // 获取body长度
        int bodyLength = body == null ? 0 : body.length;
        // 重新处理报文的总长度
        // 先保存当前的写指针的位置
        int writerIndex = byteBuf.writerIndex();
        // 将写指针的位置移动到总长度的位置上
        byteBuf.writerIndex(MessageFormatConstant.MAGIC.length
                + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH);
        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyLength);

        // 将写指针归位
        byteBuf.writerIndex(writerIndex);

    }

    /**
     * 将对象序列化为字节数组
     *
     * @param requestPayload 对象实体
     * @return 字节数组
     */
    private byte[] getBodyBytes(RequestPayload requestPayload) {
        // TODO 针对不同的消息类型需要做不同的处理，心跳的请求，没有payload
        if (requestPayload == null) {
            return null;
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(baos);
            outputStream.writeObject(requestPayload);
            // TODO 压缩
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("转换对象为字节数组失败", e);
            throw new RuntimeException(e);
        }
    }
}
