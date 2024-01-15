package com.xxxx.rpc.common;

import java.io.Serializable;
import java.util.Objects;

/**
 * 服务元数据
 */
public class ServiceMeta implements Serializable {

    private String serviceName;

    private String serviceVersion;

    private String serviceAddr;

    private int servicePort;

    /**
     * redis注册中心属性
     */
    private long endTime;

    private String UUID;

    /**
     * 合并为一个哈希码值
     * @return
     */
    @Override
    public int hashCode() {
        return Objects.hash(serviceName, serviceVersion, serviceAddr, servicePort, UUID);
    }

    /**
     * 用于故障转移，故障转移时需要移除不可用服务
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ServiceMeta that = (ServiceMeta) obj;
        return servicePort == that.servicePort &&
                Objects.equals(serviceName, that.serviceName) &&
                Objects.equals(serviceAddr, that.serviceAddr) &&
                Objects.equals(serviceVersion, that.serviceVersion) &&
                Objects.equals(UUID, that.UUID);

    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public String getServiceAddr() {
        return serviceAddr;
    }

    public void setServiceAddr(String serviceAddr) {
        this.serviceAddr = serviceAddr;
    }

    public int getServicePort() {
        return servicePort;
    }

    public void setServicePort(int servicePort) {
        this.servicePort = servicePort;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }
}
