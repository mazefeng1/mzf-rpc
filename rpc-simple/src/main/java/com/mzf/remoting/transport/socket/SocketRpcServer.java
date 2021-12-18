package com.mzf.remoting.transport.socket;

import com.mzf.config.CustomShutdownHook;
import com.mzf.config.RpcServiceConfig;
import com.mzf.factory.SingletonFactory;
import com.mzf.provider.ServiceProvider;
import com.mzf.provider.impl.ServiceProviderImpl;
import com.mzf.utils.concurrent.threadpool.ThreadPoolFactoryUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import static com.mzf.remoting.transport.netty.server.NettyRpcServer.PORT;
/**
 * @author 好大雨
 * @create 2021/9/23 23:46
 */
@Slf4j
public class SocketRpcServer {
    private final ExecutorService threadPool;
    private final ServiceProvider serviceProvider;


    public SocketRpcServer() {
        threadPool = ThreadPoolFactoryUtils.createCustomThreadPoolIfAbsent("socket-server-rpc-pool");
        serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);
    }

    public void registerService(RpcServiceConfig rpcServiceConfig) {
        serviceProvider.publishService(rpcServiceConfig);
    }

    public void start() {
        try (ServerSocket server = new ServerSocket()) {
            String host = InetAddress.getLocalHost().getHostAddress();
            server.bind(new InetSocketAddress(host, PORT));
            CustomShutdownHook.getCustomShutdownHook().clearAll();
            Socket socket;
            while ((socket = server.accept()) != null) {
                log.info("client connected [{}]", socket.getInetAddress());
                threadPool.execute(new SocketRpcRequestHandlerRunnable(socket));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            log.error("occur IOException:", e);
        }
    }
}
