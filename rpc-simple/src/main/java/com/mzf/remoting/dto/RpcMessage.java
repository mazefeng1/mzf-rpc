package com.mzf.remoting.dto;

import lombok.*;

/**
 * @author 好大雨
 * @create 2021/10/19 19:59
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcMessage {
    //rpc消息类型
    private byte messageType;
    //序列化类型
    private byte codec;
    //压缩类型
    private byte compress;
    //请求id
    private int requestId;
    //请求数据
    private Object data;
}
