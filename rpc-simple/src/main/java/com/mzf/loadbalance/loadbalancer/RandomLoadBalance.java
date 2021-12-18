package com.mzf.loadbalance.loadbalancer;

import com.mzf.loadbalance.AbstractLoadBalance;
import com.mzf.remoting.dto.RpcRequest;

import java.util.List;
import java.util.Random;

/**
 * 随机的负载均衡算法
 *
 * @author 好大雨
 * @create 2021/9/19 23:37
 */
public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        Random random = new Random();
        return serviceAddresses.get(random.nextInt(serviceAddresses.size()));
    }
}
