package com.xxxx.rpc.config;

import com.xxxx.rpc.annotation.PropertiesField;
import com.xxxx.rpc.annotation.PropertiesPrefix;
import com.xxxx.rpc.common.constants.RegistryRules;
import com.xxxx.rpc.common.constants.SerializationRules;
import lombok.Getter;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置信息
 */
@PropertiesPrefix("rpc")
public class RpcProperties {

    /**
     * netty端口
     */
    @PropertiesField
    private Integer port;

    /**
     * 注册中心地址
     */
    @PropertiesField
    private String registerAddr;

    /**
     * 注册中心类型
     */
    @PropertiesField
    private String registerType = RegistryRules.REDIS;

    /**
     * 注册中心密码
     */
    @PropertiesField
    private String registerPsw;

    /**
     * 序列化方式
     */
    @PropertiesField
    private String serialization = SerializationRules.JSON;

    /**
     * 服务端额外配置数据
     */
    @PropertiesField("service")
    private Map<String, Object> serviceAttachments = new HashMap<>();

    /**
     * 客户端额外配置数据
     */
    @PropertiesField("client")
    private Map<String, Object> clientAttachments = new HashMap<>();


    static RpcProperties rpcProperties;

    public static RpcProperties getInstance() {
        if (rpcProperties == null) {
            rpcProperties = new RpcProperties();
        }
        return rpcProperties;
    }

    public RpcProperties() {
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getRegisterAddr() {
        return registerAddr;
    }

    public void setRegisterAddr(String registerAddr) {
        this.registerAddr = registerAddr;
    }

    public String getRegisterType() {
        return registerType;
    }

    public void setRegisterType(String registerType) {
        if (registerType == null || registerType.equals("")) {
            registerType = RegistryRules.REDIS;
        }
        this.registerType = registerType;
    }

    public String getRegisterPsw() {
        return registerPsw;
    }

    public void setRegisterPsw(String registerPsw) {
        this.registerPsw = registerPsw;
    }

    public String getSerialization() {
        return serialization;
    }

    public void setSerialization(String serialization) {
        if (serialization == null || serialization.equals("")) {
            serialization = SerializationRules.JSON;
        }
        this.serialization = serialization;
    }

    public Map<String, Object> getServiceAttachments() {
        return serviceAttachments;
    }

    public void setServiceAttachments(Map<String, Object> serviceAttachments) {
        this.serviceAttachments = serviceAttachments;
    }

    public Map<String, Object> getClientAttachments() {
        return clientAttachments;
    }

    public void setClientAttachments(Map<String, Object> clientAttachments) {
        this.clientAttachments = clientAttachments;
    }

    public static void setRpcProperties(RpcProperties rpcProperties) {
        RpcProperties.rpcProperties = rpcProperties;
    }

    /**
     * 做一个能够解析任意对象属性的工具类
     * @param environment
     */
    public static void init(Environment environment){

    }

    @Override
    public String toString() {
        return "RpcProperties{" +
                "port=" + port +
                ", registerAddr='" + registerAddr + '\'' +
                ", registerType='" + registerType + '\'' +
                ", registerPsw='" + registerPsw + '\'' +
                ", serialization='" + serialization + '\'' +
                ", serviceAttachments=" + serviceAttachments +
                ", clientAttachments=" + clientAttachments +
                '}';
    }
}
