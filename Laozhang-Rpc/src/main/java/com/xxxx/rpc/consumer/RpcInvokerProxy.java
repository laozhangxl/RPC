package com.xxxx.rpc.consumer;

import com.xxxx.rpc.common.*;
import com.xxxx.rpc.common.constants.LoadBalancerRules;
import com.xxxx.rpc.common.constants.MsgType;
import com.xxxx.rpc.common.constants.ProtocolConstants;
import com.xxxx.rpc.config.RpcProperties;
import com.xxxx.rpc.filter.FilterConfig;
import com.xxxx.rpc.filter.FilterData;
import com.xxxx.rpc.protocal.MsgHeader;
import com.xxxx.rpc.protocal.RpcProtocol;
import com.xxxx.rpc.router.LoadBalancer;
import com.xxxx.rpc.router.LoadBalancerFactory;
import com.xxxx.rpc.router.ServiceMetaRes;
import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static com.xxxx.rpc.common.constants.FaultTolerantRules.*;

/**
 * 代理
 */
@Slf4j
public class RpcInvokerProxy implements InvocationHandler {

    private String serviceVersion; //服务的版本信息

    private long timeout; //RPC调用服务的超时时间

    private String loadBalancerType;//负载均衡策略

    private String faultTolerantType;//容错机制

    private long retryCount;//重试次数

    public RpcInvokerProxy(String serviceVersion, long timeout, String loadBalancerType, String faultTolerantType, long retryCount) {
        this.serviceVersion = serviceVersion;
        this.timeout = timeout;
        this.loadBalancerType = loadBalancerType;
        this.faultTolerantType = faultTolerantType;
        this.retryCount = retryCount;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
        //构建请求头
        MsgHeader header = new MsgHeader();
        long requestId = RpcRequestHolder.REQUEST_ID_GEN.incrementAndGet();
        header.setMagic(ProtocolConstants.MAGIC);
        header.setVersion(ProtocolConstants.VERSION);
        header.setRequestId(requestId);
        final byte[] serialization = RpcProperties.getInstance().getSerialization().getBytes();
        header.setSerializationLen(serialization.length);
        header.setSerializations(serialization);
        header.setMsgType((byte) MsgType.REQUEST.ordinal());
        header.setStatus((byte) 0x1);
        protocol.setHeader(header);
        //构建请求体
        RpcRequest request = new RpcRequest();
        request.setServiceVersion(this.serviceVersion);
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setData(ObjectUtils.isEmpty(args) ? new Object[0] : args);
        request.setDataClass(ObjectUtils.isEmpty(args) ? null : args[0].getClass());
        request.setServiceAttachment(RpcProperties.getInstance().getServiceAttachments());
        request.setClientAttachment(RpcProperties.getInstance().getClientAttachments());
        //拦截器的上下文
        final FilterData filterData = new FilterData(request);
        try {
            FilterConfig.getClientBeforeFilterChain().doFilter(filterData);
        } catch (Exception e) {
            throw e;
        }

        protocol.setBody(request);

        //用于发送消息
        RpcConsumer rpcConsumer = new RpcConsumer();
        
        String serviceName = RpcServiceNameBuilder.buildServiceKey(request.getClassName(), request.getServiceVersion());
        Object[] params = {request.getData()};
        //1.获取负载均衡策略
        final LoadBalancer loadBalancer = LoadBalancerFactory.get(loadBalancerType);
        //2.根据策略获取对应服务
        final ServiceMetaRes serviceMetaRes = loadBalancer.select(params, serviceName);
        ServiceMeta curServiceMeta = serviceMetaRes.getCurServiceMeta();
        final Collection<ServiceMeta> otherServiceMeta = serviceMetaRes.getOtherServiceMeta();
        long count = 1;
        long retryCount = this.retryCount;
        RpcResponse response = null;
        //重试机制
        while (count <= retryCount) {
            //处理返回函数
            RpcFuture<RpcResponse> future = new RpcFuture<>(new DefaultPromise<>(new DefaultEventLoop()), timeout);
            RpcRequestHolder.REQUEST_MAP.put(requestId, future);
            try {
                //发送消息
                rpcConsumer.sendRequest(protocol, curServiceMeta);
                //等待响应数据返回
                response = future.getPromise().get(future.getTimeout(), TimeUnit.MILLISECONDS);
                //如果有异常，并且没有其他服务
//            if (response.getException() != null && otherServiceMeta.isEmpty()) {
//                throw response.getException();
//            }
                if (response.getException() != null) {
                    throw response.getException();
                }
                log.info("rpc 调用成功, serviceName: {}", serviceName);
                try {
                    FilterConfig.getClientAfterFilterChain().doFilter(filterData);
                } catch (Exception e) {
                    throw e;
                }
                return response.getData();
            } catch (Exception e) {
                String errorMsg = e.toString();
                // todo 这里的容错机制可拓展
                switch (faultTolerantType) {
                    //快速失败
                    case FailFast:
                        log.warn("rpc 调用失败,触发 FailFast 策略,异常信息: {}", errorMsg);
                        return response.getException();
                    // 故障转移
                    case FailOver:
                        log.warn("rpc 调用失败,第{}次重试,异常信息:{}", count, errorMsg);
                        count++;
                        if (!ObjectUtils.isEmpty(otherServiceMeta)) {
                            final ServiceMeta next = otherServiceMeta.iterator().next();
                            curServiceMeta = next;
                            otherServiceMeta.remove(next);
                        } else {
                            final String msg = String.format("rpc 调用失败,无服务可用 serviceName: {%s}, 异常信息: {%s}", serviceName, errorMsg);
                            log.warn(msg);
                            throw new RuntimeException(msg);
                        }
                        break;
                    //忽略
                    case FailPass:
                        return null;
                }
            }
        }
        throw new RuntimeException("rpc 调用失败，超过最大重试次数: {}" + retryCount);
    }
}
