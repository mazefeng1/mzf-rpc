package com.mzf;

import lombok.extern.slf4j.Slf4j;

/**
 * @author 好大雨
 * @create 2021/9/24 21:28
 */
@Slf4j
public class HelloServiceImpl2 implements HelloService{
    static {
        System.out.println("HelloServiceImpl2被创建");
    }

    @Override
    public String hello(Hello hello) {
        log.info("HelloServiceImpl2收到: {}.", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        log.info("HelloServiceImpl2返回: {}.", result);
        return result;
    }
}
