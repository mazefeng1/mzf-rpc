package com.mzf.remoting.dto;

import com.mzf.enums.RpcResponseCodeEnum;
import lombok.*;

import java.io.Serializable;

/**
 *
 * @author 好大雨
 * @create 2021/9/19 19:53
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcResponse<T> implements Serializable {
    private static final long serialVersionUID = 715745410605631233L;
    private String requestId;
    //响应编码
    private Integer code;
    //响应消息
    private String message;
    //响应体
    private T data;

    /**
     *  成功响应
     * @param data
     * @param requestId
     * @param <T>
     * @return  RpcResponse<T>
     */
    public static <T> RpcResponse<T> success(T data, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(RpcResponseCodeEnum.SUCCESS.getCode());
        response.setMessage(RpcResponseCodeEnum.SUCCESS.getMessage());
        response.setRequestId(requestId);
        if (null != data) {
            response.setData(data);
        }
        return response;
    }

    /**
     * 响应失败
     * @param rpcResponseCodeEnum
     * @param <T>
     * @return
     */
    public static <T> RpcResponse<T> fail(RpcResponseCodeEnum rpcResponseCodeEnum) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(rpcResponseCodeEnum.getCode());
        response.setMessage(rpcResponseCodeEnum.getMessage());
        return response;
    }

}
