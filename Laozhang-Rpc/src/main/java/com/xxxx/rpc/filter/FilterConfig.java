package com.xxxx.rpc.filter;

import com.xxxx.rpc.spi.ExtensionLoader;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.IOException;

/**
 * @description: 拦截器配置类，用于统一管理拦截器
 */
public class FilterConfig {

    @Getter //自动生成get方法
    private static FilterChain serviceBeforeFilterChain = new FilterChain();
    @Getter
    private static FilterChain serviceAfterFilterChain = new FilterChain();
    @Getter
    private static FilterChain clientBeforeFilterChain = new FilterChain();
    @Getter
    private static FilterChain clientAfterFilterChain = new FilterChain();


    /**
     * 初始化服务端过滤器
     */
    @SneakyThrows //自动抛出任何异常，无需声明
    public static void initServiceFilter(){
        final ExtensionLoader extensionLoader = ExtensionLoader.getInstance();
        extensionLoader.loadExtension(ServiceBeforeFilter.class);
        extensionLoader.loadExtension(ServiceAfterFilter.class);
        serviceBeforeFilterChain.addFilter(extensionLoader.gets(ServiceBeforeFilter.class));
        serviceAfterFilterChain.addFilter(extensionLoader.gets(ServiceAfterFilter.class));
    }

    /**
     * 初始化客户端过滤器
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void initClientFilter() throws IOException, ClassNotFoundException {
        final ExtensionLoader extensionLoader = ExtensionLoader.getInstance();
        extensionLoader.loadExtension(ClientAfterFilter.class);
        extensionLoader.loadExtension(ClientBeforeFilter.class);
        clientAfterFilterChain.addFilter(extensionLoader.gets(ClientAfterFilter.class));
        clientBeforeFilterChain.addFilter(extensionLoader.gets(ClientBeforeFilter.class));
    }

}
