package com.mzf.registry.impl;

import com.mzf.registry.ServiceRegistry;
import com.mzf.registry.utils.CuratorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

/**
 * 服务注册实现类
 *
 * @author 好大雨
 * @create 2021/9/19 17:30
 */
@Slf4j
public class ServiceRegistryImpl implements ServiceRegistry {
    /**
     * 服务注册
     * @param rpcServiceName
     * @param inetSocketAddress
     */
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress){
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createPersistentNode(zkClient,servicePath);
    }

}
