package com.mzf.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 好大雨
 * @create 2021/9/20 21:48
 */
@AllArgsConstructor
@Getter
public enum CompressTypeEnum {
    GZIP((byte) 0x01, "gzip");

    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (CompressTypeEnum c : CompressTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }
}
