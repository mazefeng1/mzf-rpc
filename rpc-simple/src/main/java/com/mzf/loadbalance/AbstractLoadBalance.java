package com.mzf.loadbalance;

import com.mzf.remoting.dto.RpcRequest;

import java.util.List;

/**
 * 负载均衡策略
 *
 * @author 好大雨
 * @create 2021/9/19 22:50
 */
public abstract class AbstractLoadBalance implements LoadBalance{
    @Override
    public String selectServiceAddress(List<String> serviceAddresses, RpcRequest rpcRequest) {
        if (serviceAddresses == null || serviceAddresses.size() == 0) {
            return null;
        }
        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }
        return doSelect(serviceAddresses, rpcRequest);
    }

    protected abstract String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest);



}
