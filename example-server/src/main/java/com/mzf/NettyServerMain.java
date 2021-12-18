package com.mzf;

import com.mzf.annotation.RpcScan;
import com.mzf.config.RpcServiceConfig;
import com.mzf.remoting.transport.netty.server.NettyRpcServer;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author 好大雨
 * @create 2021/9/24 21:30
 */
@RpcScan(basePackage = "\\com\\mzf")
public class NettyServerMain {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyRpcServer nettyRpcServer = (NettyRpcServer) applicationContext.getBean("nettyRpcServer");
        HelloService helloService2 = new HelloServiceImpl2();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder().group("test2").version("version2").service(helloService2).build();
        nettyRpcServer.registerService(rpcServiceConfig);
        nettyRpcServer.start();
    }
}
