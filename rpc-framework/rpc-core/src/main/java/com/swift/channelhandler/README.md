1、服务调用方
发送报文 writeAndFlush(object)  请求
此object应该是什么？ 应该包含一些什么样的信息？
rpcRequest （
1、请求id （long）
2、压缩类型 （1byte） 3、序列化的方式 （1byte） 4、消息类型（普通请求，心跳检测请求）（1byte）  
5、负载 payload（接口的名字，方法的名字，参数列表，返回值类型））

pipeline就生效了，报文开始出站
---> 第一个处理器 in/out log
---> 第二个处理器 编码器（out）（转化 yrpcrequest -> msg(请求报文)，序列化，压缩）

2、服务提供方
通过netty接受报文
pipeline就生效了，报文开始出站
---> 第一个处理器 in/out log   
---> 第二个处理器 解码器（in）（解压缩，反序列化，msg-> yrpcrequest）
---> 想办法处理 （in） yrpcrequest 执行方法调用，得到结果

3、 执行方法调用，得到结果

4、服务提供方
发送报文 writeAndFlush(object) 响应
pipeline就生效了，报文开始出站
---> 第一个处理器（out）（转化 object -> msg(响应报文)）  
---> 第二个处理器（out）（序列化）
---> 第三个处理器（out）（压缩）

5、服务调用方
通过netty接受响应报文
pipeline就生效了，报文开始出站
---> 第一个处理器（in）（解压缩）  
---> 第二个处理器（in）（反序列化）
---> 第三个处理器（in）（解析报文）

6、得到结果返回

