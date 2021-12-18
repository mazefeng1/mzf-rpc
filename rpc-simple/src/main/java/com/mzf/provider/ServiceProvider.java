package com.mzf.provider;

import com.mzf.config.RpcServiceConfig;

/**
 *  存储提供服务
 * @author 好大雨
 * @create 2021/9/20 15:42
 */
public interface ServiceProvider {
    /**
     * 添加服务
     * @param rpcServiceConfig
     */
    void addService(RpcServiceConfig rpcServiceConfig);

    /**
     * 获取服务
     * @param rpcServiceName
     * @return
     */
    Object getService(String rpcServiceName);

    /**
     * 发布服务
     * @param rpcServiceConfig
     */
    void publishService(RpcServiceConfig rpcServiceConfig);
}
