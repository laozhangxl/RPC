package com.xxxx.rpc.poll;

import com.xxxx.rpc.common.RpcRequest;
import com.xxxx.rpc.common.RpcResponse;
import com.xxxx.rpc.common.RpcServiceNameBuilder;
import com.xxxx.rpc.common.constants.MsgStatus;
import com.xxxx.rpc.common.constants.MsgType;
import com.xxxx.rpc.protocal.MsgHeader;
import com.xxxx.rpc.protocal.RpcProtocol;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池工厂
 */
public class ThreadPollFactory {

    private static Logger logger = LoggerFactory.getLogger(ThreadPollFactory.class);

    private static ThreadPoolExecutor slowPoll;
    private static ThreadPoolExecutor fastPoll;

    private static volatile ConcurrentHashMap<String, AtomicInteger> slowTaskMap = new ConcurrentHashMap<>();

    //处理器的核心数
    private static int corSize = Runtime.getRuntime().availableProcessors();

    //存储rpc服务的映射关系，用于缓存服务
    private static Map<String, Object> rpcServiceMap;

    static {
        //初始化线程池
        slowPoll = new ThreadPoolExecutor(corSize / 2, corSize, 60L,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(2000),
                r -> {
                    Thread thread = new Thread(r);
                    thread.setName("slow poll-" + r.hashCode());
                    thread.setDaemon(true);
                    return thread;
                });
        fastPoll = new ThreadPoolExecutor(corSize, corSize * 2, 60L,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(1000),
                r -> {
                    Thread thread = new Thread(r);
                    thread.setName("fast poll-" + r.hashCode());
                    thread.setDaemon(true);
                    return thread;
                });
        //清理慢请求
        startClearMonitor();
    }

    public ThreadPollFactory() {
    }

    public static void setRpcServiceMap(Map<String, Object> rpcServiceMap) {
        ThreadPollFactory.rpcServiceMap = rpcServiceMap;
    }

    /**
     * 清理慢请求
     */
    public static void startClearMonitor() {
        //定期执行任务
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            //定期清理map集合的任务
            slowTaskMap.clear();
        }, 5, 5, TimeUnit.MINUTES);
    }

    public static void submitRequest(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol){
        RpcRequest request = protocol.getBody();
        String key = request.getClassName() + request.getMethodName() + request.getServiceVersion();
        ThreadPoolExecutor pool = fastPoll;
        if (slowTaskMap.containsKey(key) && slowTaskMap.get(key).intValue() >= 10) {
            pool = slowPoll;
        }
        pool.submit(() -> {
            RpcProtocol<RpcResponse> resProtocol = new RpcProtocol<>();
            final MsgHeader header = protocol.getHeader();
            RpcResponse response = new RpcResponse();
            long startTime = System.currentTimeMillis();
            try {
                final Object result = submit(ctx, protocol);
                response.setData(result);
                response.setDataClass(result == null ? null : result.getClass());
                header.setStatus((byte) MsgStatus.SUCCESS.ordinal());
            } catch (Exception e) {
                header.setStatus((byte) MsgStatus.FAILED.ordinal());
                response.setException(e);
                logger.error("process request {} error", header.getRequestId(), e);
            } finally {
                long cost = System.currentTimeMillis() - startTime;
                System.out.println("cost time:" + cost);
                if (cost > 1000) {
                    final AtomicInteger timeoutCount = slowTaskMap.putIfAbsent(key, new AtomicInteger(1));
                    if (timeoutCount == null) {
                        timeoutCount.incrementAndGet();
                    }
                }
            }
            resProtocol.setHeader(header);
            resProtocol.setBody(response);
            logger.info("执行成功: {},{},{},{}",Thread.currentThread().getName(),request.getClassName(),request.getMethodName(),request.getServiceVersion());
            ctx.fireChannelRead(resProtocol);

        });
    }

    /**
     * 处理RPC请求
     * @param ctx
     * @param protocol
     * @return
     * @throws Exception
     */
    private static Object submit(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) throws Exception{
        RpcProtocol<RpcResponse> resProtocol = new RpcProtocol<>();
        MsgHeader header = protocol.getHeader();
        header.setMsgType((byte) MsgType.RESPONSE.ordinal());
        RpcRequest request = protocol.getBody();
        //执行业务
        return handle(request);
    }

    /**
     * 反射调用提供方的指定方法
     * @param request
     * @return
     * @throws Exception
     */
    private static Object handle(RpcRequest request) throws Exception {
        String serviceKey = RpcServiceNameBuilder.buildServiceKey(request.getClassName(), request.getServiceVersion());
        //获取服务信息
        Object serviceBean = rpcServiceMap.get(serviceKey);
        if (serviceBean == null) {
            throw new RuntimeException(String.format("service not exist: %s:%s", request.getClassName(), request.getMethodName()));
        }
        // 获取服务提供方信息并且创建
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = {request.getData()};

        // FastClass 工具类创建服务类的快速类
        FastClass fastClass = FastClass.create(serviceClass);
        //获取指定方法索引
        int methodIndex = fastClass.getIndex(methodName, parameterTypes);
        //调用方法返回结果
        return fastClass.invoke(methodIndex, serviceBean, parameters);
    }
    
        
    
}
