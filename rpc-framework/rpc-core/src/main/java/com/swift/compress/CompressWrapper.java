package com.swift.compress;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author sunGuoNan
 * @version 1.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CompressWrapper {
    private byte code;
    private String type;
    private Compressor compressor;
}
