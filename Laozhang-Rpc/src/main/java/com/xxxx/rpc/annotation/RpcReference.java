package com.xxxx.rpc.annotation;

import com.xxxx.rpc.common.constants.FaultTolerantRules;
import com.xxxx.rpc.common.constants.LoadBalancerRules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD) // 只能标注与某个类
@Retention(RetentionPolicy.RUNTIME) //在运行时可以通过反射处理该注解
public @interface RpcReference {

    /**
     * 版本
     * @return
     */
    String serviceVersion() default "1.0";

    /**
     * 超时时间
     * @return
     */
    long timeout() default 5000; //毫秒

    /**
     * 可选的负载均衡策略
     * @return
     */
    String loadBalancer() default LoadBalancerRules.RoundRobin;

    /**
     * 可选的容错策略
     * @return
     */
    String faultTolerant() default FaultTolerantRules.FailFast;

    /**
     * 重试次数
     * @return
     */
    long retryCount() default 3;
}
