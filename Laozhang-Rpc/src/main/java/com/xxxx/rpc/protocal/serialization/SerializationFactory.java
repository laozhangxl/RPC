package com.xxxx.rpc.protocal.serialization;


import com.xxxx.rpc.spi.ExtensionLoader;

/**
 * @description: 序列化工厂
 */
public class SerializationFactory {


    public static RpcSerialization get(String serialization) throws Exception {

        return ExtensionLoader.getInstance().get(serialization);

    }

    public static void init() throws Exception {
        ExtensionLoader.getInstance().loadExtension(RpcSerialization.class);
    }
}
