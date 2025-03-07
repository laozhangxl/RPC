package com.xxxx.rpc.annotation;

import com.xxxx.rpc.provider.ProviderPostProcessor;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开启服务提供方自动装配
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(ProviderPostProcessor.class)
public @interface EnableProviderRpc {
}
