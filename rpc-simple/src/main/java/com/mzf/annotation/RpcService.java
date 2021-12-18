package com.mzf.annotation;

import java.lang.annotation.*;

/**
 * 标识 RPC服务类的注解
 *
 * @author 好大雨
 * @create 2021/9/16 21:52
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RpcService {
    String version() default "";
    String group() default "";
}
