package com.xxxx.rpc.common;

import java.io.Serializable;

/**
 * 响应信息类
 */
public class RpcResponse implements Serializable {

    //返回响应的数据
    private Object data;

    //响应的数据类型
    private Class dataClass;

    //Rpc响应状态
    private String message;

    //Rpc调用过程中的异常信息
    private Exception exception;

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Class getDataClass() {
        return dataClass;
    }

    public void setDataClass(Class dataClass) {
        this.dataClass = dataClass;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
