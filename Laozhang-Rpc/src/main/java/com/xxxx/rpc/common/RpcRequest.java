package com.xxxx.rpc.common;

import java.io.Serializable;
import java.util.Map;

/**
 * 请求体
 */
public class RpcRequest implements Serializable {

    //服务版本
    private String serviceVersion;

    //调用的服务类的名称
    private String className;

    //调用的方法名
    private String methodName;

    //请求的数据
    private Object data;

    //数据中的类
    private Class dataClass;

    //方法的参数
    private Class<?>[] parameterTypes;

    //服务相关附加信息
    private Map<String, Object> serviceAttachment;

    //客户端相关附加信息
    private Map<String, Object> clientAttachment;

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Class getDataClass() {
        return dataClass;
    }

    public void setDataClass(Class dataClass) {
        this.dataClass = dataClass;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Map<String, Object> getServiceAttachment() {
        return serviceAttachment;
    }

    public void setServiceAttachment(Map<String, Object> serviceAttachment) {
        this.serviceAttachment = serviceAttachment;
    }

    public Map<String, Object> getClientAttachment() {
        return clientAttachment;
    }

    public void setClientAttachment(Map<String, Object> clientAttachment) {
        this.clientAttachment = clientAttachment;
    }
}
