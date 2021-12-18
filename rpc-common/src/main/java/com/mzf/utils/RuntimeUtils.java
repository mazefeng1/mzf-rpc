package com.mzf.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author 好大雨
 * @create 2021/9/19 11:25
 */

public class RuntimeUtils {
    /**
     * 获取CPU的核心数
     * @return cpu的核心数
     */
    public static int cpus() {
        return Runtime.getRuntime().availableProcessors();
    }
}
