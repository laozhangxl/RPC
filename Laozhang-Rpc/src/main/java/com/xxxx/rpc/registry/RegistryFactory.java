package com.xxxx.rpc.registry;

import com.xxxx.rpc.spi.ExtensionLoader;

import java.io.IOException;

/**
 * 注册工厂
 */
public class RegistryFactory {

    public static RegistryService get(String registryService) throws Exception {
        return ExtensionLoader.getInstance().get(registryService);
    }

    public static void init() throws Exception {
        ExtensionLoader.getInstance().loadExtension(RegistryFactory.class);
    }

}
