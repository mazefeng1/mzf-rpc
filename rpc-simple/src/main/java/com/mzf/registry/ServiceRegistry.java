package com.mzf.registry;

import com.mzf.extension.SPI;

import java.net.InetSocketAddress;

/**
 * 服务注册接口
 *
 * @author 好大雨
 * @create 2021/9/19 17:24
 */
@SPI
public interface ServiceRegistry {
    /**
     * 服务注册
     * @param rpcServiceName
     * @param inetSocketAddress
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);

}
