package com.swift.transport.message;

/**
 * <pre>
 *  0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21   22   23
 *  +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+
 *  |    magic               |ver |head len|    full length    | qt | ser|comp|              RequestId                 |
 *  +-----+-----+-------+----+----+----+----+-----------+----- ---+--------+----+----+----+----+----+----+---+---+---+-+
 *  |                                                                                                                  |
 *  |                                         body                                                                     |
 *  |                                                                                                                  |
 *  +--------------------------------------------------------------------------------------------------------+---+---+-+
 *  </pre>
 *
 * @author sunGuoNan
 * @version 1.0
 */
public class MessageFormatConstant {
    // 定义魔术值
    public final static byte[] MAGIC = "swift".getBytes();
    // 定义版本长度
    public final static byte VERSION = 1;
    // 头部信息的长度
    public final static short HEADER_LENGTH = (byte) (MAGIC.length + 1 + 2 + 4 + 1 + 1 + 1 + 8);
    // 头部信息长度占用的字节数
    public static final int HEADER_FIELD_LENGTH = 2;
    // 最大帧的长度
    public final static int MAX_FRAME_LENGTH = 1024 * 1024;
    // 版本长度
    public static final int VERSION_LENGTH = 1;
    // 总长度占用的字节数
    public static final int FULL_FIELD_LENGTH = 4;
}
