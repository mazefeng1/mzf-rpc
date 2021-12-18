package com.mzf.registry.impl;

import com.mzf.enums.RpcErrorMessageEnum;
import com.mzf.exception.RpcException;
import com.mzf.extension.ExtensionLoader;
import com.mzf.loadbalance.LoadBalance;
import com.mzf.registry.ServiceDiscovery;
import com.mzf.registry.utils.CuratorUtils;
import com.mzf.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 服务发现实现类
 *
 * @author 好大雨
 * @create 2021/9/19 20:02
 */
@Slf4j
public class ServiceDiscoveryImpl implements ServiceDiscovery {

    private final LoadBalance loadBalance;

    public ServiceDiscoveryImpl() {
        this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension("loadBalance");
    }
    /**
     *  查询服务的地址
     * @param rpcRequest
     * @return InetSocketAddress
     */
    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);

        if (serviceUrlList == null || serviceUrlList.size() == 0) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcServiceName);
        }
        // 对服务列表进行负载均衡
        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList, rpcRequest);
        log.info("成功找到服务地址:[{}]", targetServiceUrl);
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}
