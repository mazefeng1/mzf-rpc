package com.mzf.spring;

import com.mzf.annotation.RpcReference;
import com.mzf.annotation.RpcService;
import com.mzf.config.RpcServiceConfig;
import com.mzf.extension.ExtensionLoader;
import com.mzf.factory.SingletonFactory;
import com.mzf.provider.ServiceProvider;
import com.mzf.provider.impl.ServiceProviderImpl;
import com.mzf.proxy.RpcClientProxy;
import com.mzf.remoting.transport.RpcRequestTransport;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 *
 * @author 好大雨
 * @create 2021/9/16 23:55
 */
@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {
    private final ServiceProvider serviceProvider;
    private final RpcRequestTransport rpcClient;

    public SpringBeanPostProcessor() {
        this.serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);
        this.rpcClient = ExtensionLoader.getExtensionLoader(RpcRequestTransport.class).getExtension("netty");
    }

    /**
     * bean初始化前调用，为服务提供者完成注册
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            log.info("[{}] is annotated with  [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());
            //暴露服务的类
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            // 将相关配置记录注册到registry中
            RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                    .group(rpcService.group())
                    .version(rpcService.version())
                    .service(bean).build();
            serviceProvider.publishService(rpcServiceConfig);
        }
        return bean;
    }

    /**
     * bean初始化后，为服务消费者设置动态代理
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            //引用服务的属性
            RpcReference rpcReference = declaredField.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                        .group(rpcReference.group())
                        .version(rpcReference.version()).build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceConfig);
                Object clientProxy = rpcClientProxy.getProxy(declaredField.getType());
                declaredField.setAccessible(true);
                try {
                    declaredField.set(bean, clientProxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } finally {
                    declaredField.setAccessible(false);
                }
            }

        }
        return bean;
    }
}
