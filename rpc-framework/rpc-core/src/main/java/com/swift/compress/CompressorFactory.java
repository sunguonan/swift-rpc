package com.swift.compress;


import com.swift.compress.impl.GzipCompressor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sunGuoNan
 * @version 1.0
 */
@Slf4j
public class CompressorFactory {

    private final static ConcurrentHashMap<String, CompressWrapper> COMPRESSOR_CACHE = new ConcurrentHashMap<>(8);
    private final static ConcurrentHashMap<Byte, CompressWrapper> COMPRESSOR_CACHE_CODE = new ConcurrentHashMap<>(8);

    static {
        CompressWrapper gzip = new CompressWrapper((byte) 1, "gzip", new GzipCompressor());
        COMPRESSOR_CACHE.put("gzip", gzip);
        COMPRESSOR_CACHE_CODE.put((byte) 1, gzip);
    }

    /**
     * 使用工厂方法获取一个CompressorWrapper
     *
     * @param compressorType 序列化的类型
     * @return CompressWrapper
     */
    public static CompressWrapper getCompressor(String compressorType) {
        CompressWrapper compressWrapper = COMPRESSOR_CACHE.get(compressorType);
        if (compressWrapper == null) {
            log.error("未找到您配置的【{}】压缩算法，默认选用gzip算法。", compressorType);
            return COMPRESSOR_CACHE.get("gzip");
        }
        return compressWrapper;
    }

    public static CompressWrapper getCompressor(byte serializeCode) {
        CompressWrapper compressWrapper = COMPRESSOR_CACHE_CODE.get(serializeCode);
        if ((compressWrapper == null)) {
            log.error("未找到您配置的编号为【{}】的压缩算法，默认选用gzip算法。", serializeCode);
            return COMPRESSOR_CACHE.get("gzip");
        }
        return compressWrapper;
    }
}
