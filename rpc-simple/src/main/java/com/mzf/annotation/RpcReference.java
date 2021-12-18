package com.mzf.annotation;

import java.lang.annotation.*;

/**
 * RPC引用（消费者）注释，自动连接服务实现类
 *
 * @author 好大雨
 * @create 2021/9/16 22:07
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RpcReference {
    String version() default "";
    String group() default "";
}
