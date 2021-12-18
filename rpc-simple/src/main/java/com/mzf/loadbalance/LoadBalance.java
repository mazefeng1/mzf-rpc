package com.mzf.loadbalance;

import com.mzf.extension.SPI;
import com.mzf.remoting.dto.RpcRequest;

import java.util.List;

/**
 * 负载均衡算法接口
 * @author 好大雨
 * @create 2021/9/19 22:26
 */
@SPI
public interface LoadBalance {
    /**
     *   通过负载均衡从服务列表中选取一个服务
     * @param serviceAddresses
     * @param rpcRequest
     * @return
     */
    String selectServiceAddress(List<String> serviceAddresses, RpcRequest rpcRequest);
}
