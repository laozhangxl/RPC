package com.xxxx.rpc.protocal;

import java.io.Serializable;

/**
 * 消息
 * @param <T>
 */
public class RpcProtocol<T> implements Serializable {

    //消息头
    private MsgHeader header;

    //消息体
    private T body;

    public MsgHeader getHeader() {
        return header;
    }

    public void setHeader(MsgHeader header) {
        this.header = header;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}
