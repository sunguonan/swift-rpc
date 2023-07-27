package com.swift;

import com.swift.annotation.TryTimes;

/**
 * @author sunGuoNan
 * @version 1.0
 */
public interface HelloRpc2 {
    @TryTimes
    String sayHi(String msg);
}
