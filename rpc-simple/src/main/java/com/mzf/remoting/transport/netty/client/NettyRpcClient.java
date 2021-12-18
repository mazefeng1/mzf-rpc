package com.mzf.remoting.transport.netty.client;

import com.mzf.enums.CompressTypeEnum;
import com.mzf.enums.SerializationTypeEnum;
import com.mzf.extension.ExtensionLoader;
import com.mzf.factory.SingletonFactory;
import com.mzf.registry.ServiceDiscovery;
import com.mzf.remoting.constants.RpcConstants;
import com.mzf.remoting.dto.RpcMessage;
import com.mzf.remoting.dto.RpcRequest;
import com.mzf.remoting.dto.RpcResponse;
import com.mzf.remoting.transport.RpcRequestTransport;
import com.mzf.remoting.transport.netty.codec.RpcMessageDecoder;
import com.mzf.remoting.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 初始化、关闭bootstrap对象
 *
 * @author 好大雨
 * @create 2021/9/22 19:49
 */
@Slf4j
public final class NettyRpcClient implements RpcRequestTransport {
    private final ServiceDiscovery serviceDiscovery;
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    private final UnprocessedRequests unprocessedRequests;
    private final ChannelProvider channelProvider;
    public NettyRpcClient() {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS))
                                .addLast(new RpcMessageEncoder())
                                .addLast(new RpcMessageDecoder())
                                .addLast(new NettyRpcClientHandler());
                    }
                });
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        CompletableFuture <RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        Channel channel = getChannel(inetSocketAddress);
        if(channel.isActive()){
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            RpcMessage rpcMessage = RpcMessage.builder().data(rpcRequest)
                    .codec(SerializationTypeEnum.PROTOSTUFF.getCode())
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .messageType(RpcConstants.REQUEST_TYPE).build();
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("client send message: [{}]", rpcMessage);
                } else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("Send failed:", future.cause());
                }
            });
        }else {
            throw new IllegalStateException();
        }
        return resultFuture;
    }

    /**
     * 获取channel
     * @param inetSocketAddress
     * @return channel
     */
    Channel getChannel(InetSocketAddress inetSocketAddress) {
        Channel channel = channelProvider.get(inetSocketAddress);
        if (channel == null) {
            channel = doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }

    /**
     * 连接服务端，获取channel
     * @param inetSocketAddress
     * @return channel
     */
    @SneakyThrows
    private Channel doConnect(InetSocketAddress inetSocketAddress) {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("The client has connected [{}] successful!", inetSocketAddress.toString());
                completableFuture.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();
    }
}
