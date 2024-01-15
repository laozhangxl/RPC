package com.xxxx.rpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE) //用于类标记
@Retention(RetentionPolicy.RUNTIME) //运行时被发现，反射处理
@Component //被Spring容器进行管理
public @interface RpcService {

    /**
     * 版本
     * @return
     */
    String serviceVersion() default "1.0";

    /**
     * 指定实现方，默认为第一个实现的接口
     * @return
     */
    Class<?> serviceInterface() default void.class;

}
