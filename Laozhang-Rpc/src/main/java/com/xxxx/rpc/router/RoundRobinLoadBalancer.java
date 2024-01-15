package com.xxxx.rpc.router;

import com.xxxx.rpc.common.ServiceMeta;
import com.xxxx.rpc.config.RpcProperties;
import com.xxxx.rpc.registry.RegistryService;
import com.xxxx.rpc.spi.ExtensionLoader;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询算法
 */
public class RoundRobinLoadBalancer implements LoadBalancer {
    
    private static AtomicInteger roundRobinId = new AtomicInteger(0);
    
    @Override
    public ServiceMetaRes select(Object[] params, String serviceName) {
        //获取注册中心
        RegistryService registryService = ExtensionLoader.getInstance().get(RpcProperties.getInstance().getRegisterType());
        List<ServiceMeta> discoveries = registryService.discoveries(serviceName);
        //1. 获取所有服务
        int size = discoveries.size();
        roundRobinId.addAndGet(1);
        if (roundRobinId.get() == Integer.MAX_VALUE) {
            roundRobinId.set(0);
        }
        return ServiceMetaRes.build(discoveries.get(roundRobinId.get() % size), discoveries);
    }
}
