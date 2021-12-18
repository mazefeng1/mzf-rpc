package com.mzf;

import com.mzf.config.RpcServiceConfig;
import com.mzf.proxy.RpcClientProxy;
import com.mzf.remoting.transport.RpcRequestTransport;
import com.mzf.remoting.transport.socket.SocketRpcClient;

/**
 * @author 好大雨
 * @create 2021/9/24 22:01
 */
public class SocketClientMain {
    public static void main(String[] args) {
        RpcRequestTransport rpcRequestTransport = new SocketRpcClient();
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcRequestTransport, rpcServiceConfig);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.hello(new Hello("111", "222"));
        System.out.println(hello);
    }
}
