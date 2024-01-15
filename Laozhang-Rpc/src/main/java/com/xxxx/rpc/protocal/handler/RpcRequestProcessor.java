package com.xxxx.rpc.protocal.handler;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 业务线程池：处理并发的RPC请求
 */
public class RpcRequestProcessor {
    
    private static ThreadPoolExecutor threadPoolExecutor;
    
    public static void submitRequest(Runnable task) {
        //线程池为空，进入同步块
        if (threadPoolExecutor == null) {
            synchronized (RpcRequestProcessor.class) {
                //再次检查，确保只有一个线程进入
                if (threadPoolExecutor == null) {
                    //创建线程池
                    threadPoolExecutor = new ThreadPoolExecutor(10, 10, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10000));
                }
            }
        }
        //提交任务，线程池执行
        threadPoolExecutor.submit(task);
    }
    
}
