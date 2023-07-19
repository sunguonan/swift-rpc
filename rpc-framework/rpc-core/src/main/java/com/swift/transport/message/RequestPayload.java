package com.swift.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用来描述，调用方所请求的 接口方法的描述
 * helloRpc.sayHi("hello rpc");
 *
 * @author sunGuoNan
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestPayload implements Serializable {

    private static final long serialVersionUID = 2011280160361277105L;

    // 1、接口的名字 -- com.swift.HelloRpc
    private String interfaceName;

    // 2、方法的名字 --sayHi
    private String methodName;

    // 3、参数列表，参数分为参数类型和具体的参数
    // 参数类型用来确定重载方法，具体的参数用来执行方法调用
    private Class<?>[] parametersType;  // -- {java.long.String}
    private Object[] parametersValue;   // -- "hello rpc"

    // 4、返回值的封装 -- {java.long.String}
    private Class<?> returnType;

}
