package com.mzf;

import com.mzf.config.RpcServiceConfig;
import com.mzf.remoting.transport.socket.SocketRpcServer;

/**
 * @author 好大雨
 * @create 2021/9/24 21:39
 */
public class SocketServerMain {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        SocketRpcServer socketRpcServer = new SocketRpcServer();
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        rpcServiceConfig.setService(helloService);
        socketRpcServer.registerService(rpcServiceConfig);
        socketRpcServer.start();
    }
}
