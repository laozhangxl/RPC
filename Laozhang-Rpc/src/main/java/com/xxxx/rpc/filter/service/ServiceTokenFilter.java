package com.xxxx.rpc.filter.service;

import com.xxxx.rpc.config.RpcProperties;
import com.xxxx.rpc.filter.FilterData;
import com.xxxx.rpc.filter.ServiceBeforeFilter;

import java.util.Map;

/**
 * token拦截器
 */
public class ServiceTokenFilter implements ServiceBeforeFilter {
    
    
    @Override
    public void doFilter(FilterData filterData) {
        final Map<String, Object> serviceAttachments = RpcProperties.getInstance().getServiceAttachments();
        final Map<String, Object> attachments = filterData.getClientAttachments();
        if (!attachments.getOrDefault("token", "").equals(serviceAttachments.getOrDefault("token", ""))) {
            throw new IllegalArgumentException("token 不正确....");
        }
        
    }
}
