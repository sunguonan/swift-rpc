package com.swift.channelhandler.handler;

import com.swift.serialize.Serializer;
import com.swift.serialize.SerializerFactory;
import com.swift.transport.message.MessageFormatConstant;
import com.swift.transport.message.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * <pre>
 *   0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21   22   23
 * +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+
 * |    magic               |ver |head len|    full length    | code | ser|comp|              RequestId               |
 * +-----+-----+-------+----+----+----+----+-----------+----- ---+--------+----+----+----+----+----+----+---+---+---+-+
 * |                                                                                                                  |
 * |                                         body                                                                     |
 * |                                                                                                                  |
 * +--------------------------------------------------------------------------------------------------------+---+---+-+
 *  </pre>
 *
 * @author sunGuoNan
 * @version 1.0
 */
@Slf4j
public class RpcResponseEncoder extends MessageToByteEncoder<RpcResponse> {
    /**
     * 将需要发送的消息进行编码 转化为二进制数组
     *
     * @param channelHandlerContext
     * @param rpcResponse
     * @param byteBuf
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse, ByteBuf byteBuf) throws Exception {
        // 5个字节的魔术值
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        // 1个字节的版本号
        byteBuf.writeByte(MessageFormatConstant.VERSION);
        // 2个字节的头部的长度
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
        // 总长度不清楚，不知道body的长度 writeIndex(写指针)
        byteBuf.writerIndex(byteBuf.writerIndex() + MessageFormatConstant.FULL_FIELD_LENGTH);
        // 3个类型
        byteBuf.writeByte(rpcResponse.getCode());
        byteBuf.writeByte(rpcResponse.getSerializeType());
        byteBuf.writeByte(rpcResponse.getCompressType());
        // 8字节的请求id
        byteBuf.writeLong(rpcResponse.getRequestId());

        // 写入请求体（requestPayload）
        Serializer serializer = SerializerFactory.getSerializer(rpcResponse.getSerializeType()).getSerializer();
        byte[] body = serializer.serialize(rpcResponse.getBody());

        // TODO 压缩 
        
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

        log.debug("响应【{}】已经在服务端完成编码工作。", rpcResponse.getRequestId());
    }
}
