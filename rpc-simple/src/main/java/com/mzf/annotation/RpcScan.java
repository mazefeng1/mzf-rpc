package com.mzf.annotation;

import com.mzf.spring.CustomScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 扫描自定义注解
 *
 * @author 好大雨
 * @create 2021/9/16 21:11
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(CustomScannerRegistrar.class)
@Documented
public @interface RpcScan {
    String[] basePackage();
}
