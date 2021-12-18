package com.mzf.spring;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;

/**
 * 自定义包扫描器
 *
 * @author 好大雨
 * @create 2021/9/16 22:17
 */
public class CustomScanner extends ClassPathBeanDefinitionScanner {
    public CustomScanner(BeanDefinitionRegistry registry , Class<? extends Annotation> annotationType){
        super(registry);
        super.addIncludeFilter(new AnnotationTypeFilter(annotationType));
    }

    @Override
    public int scan(String... basePackages){
        return super.scan(basePackages);
    }
}
