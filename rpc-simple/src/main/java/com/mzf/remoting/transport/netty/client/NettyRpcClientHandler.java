package com.mzf.remoting.transport.netty.client;

import com.mzf.enums.CompressTypeEnum;
import com.mzf.enums.SerializationTypeEnum;
import com.mzf.factory.SingletonFactory;
import com.mzf.remoting.constants.RpcConstants;
import com.mzf.remoting.dto.RpcMessage;
import com.mzf.remoting.dto.RpcResponse;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 *
 * @author 好大雨
 * @create 2021/9/23 21:55
 */
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {
    private final NettyRpcClient nettyRpcClient;
    private final UnprocessedRequests unprocessedRequests;

    public NettyRpcClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }

    /**
     * 读事件发生
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        try{
            log.info("client receive msg:[{}]",msg);
            if(msg instanceof RpcMessage){
                RpcMessage tmp = ((RpcMessage) msg);
                byte messageType = tmp.getMessageType();
                if(messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE){
                    //服务端的心跳响应
                    log.info("heart[{}]",tmp.getData());
                }else if(messageType == RpcConstants.RESPONSE_TYPE){
                    //服务端的消息响应
                    RpcResponse<Object> response = ((RpcResponse<Object>) tmp.getData());
                    unprocessedRequests.complete(response);
                }else{
                    ctx.fireChannelRead(msg);
                }
            }
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 处理IdleStateEvent事件(超时检测)
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            //如果在指定的时间周期内,没有读操作发生
            if (state == IdleState.WRITER_IDLE) {
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                rpcMessage.setMessageType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
                rpcMessage.setData(RpcConstants.PING);
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 异常捕获
     * @param ctx
     * @param cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("client catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
