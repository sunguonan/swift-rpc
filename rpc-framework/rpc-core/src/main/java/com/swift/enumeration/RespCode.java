package com.swift.enumeration;

/**
 * @author sunGuoNan
 * @version 1.0
 */
public enum RespCode {
    SUCCESS((byte) 1, "成功"), FAIL((byte) 2, "失败");

    private byte code;
    private String desc;

    RespCode(byte code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public byte getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
