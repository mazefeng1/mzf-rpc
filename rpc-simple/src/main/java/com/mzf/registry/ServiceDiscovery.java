package com.mzf.registry;

import com.mzf.extension.SPI;
import com.mzf.remoting.dto.RpcRequest;
import java.net.InetSocketAddress;

/**
 * 服务发现接口
 *
 * @author 好大雨
 * @create 2021/9/19 19:43
 */
@SPI
public interface ServiceDiscovery {
    /**
     *  查询服务的地址
     * @param rpcRequest
     * @return InetSocketAddress
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
