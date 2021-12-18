package com.mzf.remoting.transport;

import com.mzf.extension.SPI;
import com.mzf.remoting.dto.RpcRequest;

/**
 *  发送rpc请求
 * @author 好大雨
 * @create 2021/10/20 17:31
 */
@SPI
public interface RpcRequestTransport {
    /**
     * 发送请求，获取结果
     * @param rpcRequest
     * @return result from server
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
