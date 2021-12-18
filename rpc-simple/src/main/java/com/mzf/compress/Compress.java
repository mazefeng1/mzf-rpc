package com.mzf.compress;

import com.mzf.extension.SPI;

/**
 * @author 好大雨
 * @create 2021/9/21 15:42
 */
@SPI
public interface Compress {
    byte[] compress(byte[] bytes);

    byte[] decompress(byte[] bytes);
}
