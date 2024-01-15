package com.xxxx.rpc.common;

import io.netty.util.concurrent.Promise;

/**
 * Rpc调用返回结果，可以异步等待，不用等待阻塞
 * @param <T>
 */
public class RpcFuture<T> {
    
    //表示异步操作最终结果或错误的对象
    private Promise<T> promise;
    
    //超时时间
    private long timeout;

    public Promise<T> getPromise() {
        return promise;
    }

    public void setPromise(Promise<T> promise) {
        this.promise = promise;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public RpcFuture(Promise<T> promise, long timeout) {
        this.promise = promise;
        this.timeout = timeout;
    }
}


