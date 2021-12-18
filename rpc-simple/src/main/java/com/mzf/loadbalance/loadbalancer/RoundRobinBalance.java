package com.mzf.loadbalance.loadbalancer;

import com.mzf.loadbalance.AbstractLoadBalance;
import com.mzf.remoting.dto.RpcRequest;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询
 *
 * @author 好大雨
 * @create 2021/9/19 23:40
 */
public class RoundRobinBalance extends AbstractLoadBalance{
    private AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        int index = getAndIncrement() % serviceAddresses.size();
        return serviceAddresses.get(index);
    }

    /**
     * 防止数组越界
     * @return
     */
    private final int getAndIncrement() {
        int current;
        int next;
        do {
            current = this.atomicInteger.get();
            next = current >= 2147483647 ? 0 : current + 1;
        } while (!this.atomicInteger.compareAndSet(current, next));
        return next;
    }
}
