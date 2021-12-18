package com.mzf.exception;

import com.mzf.enums.RpcErrorMessageEnum;

/**
 * @author 好大雨
 * @create 2021/9/19 11:55
 */
public class RpcException extends RuntimeException{
    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum,String details){
        super(rpcErrorMessageEnum.getMessage()+":"+details);
    }
    public RpcException(String message,Throwable cause){
        super(message, cause);
    }
    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum){
        super(rpcErrorMessageEnum.getMessage());
    }
}
