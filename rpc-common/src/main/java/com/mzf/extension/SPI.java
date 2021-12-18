package com.mzf.extension;

import java.lang.annotation.*;

/**
 * SPI机制，service provider interface
 * 用于标识服务接口
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SPI {
}
