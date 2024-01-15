package com.xxxx.rpc.router;

import com.xxxx.rpc.common.ServiceMeta;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 用于对服务的管理
 */
public class ServiceMetaRes {

    // 当前服务节点
    private ServiceMeta curServiceMeta;

    // 剩余服务节点
    private Collection<ServiceMeta> otherServiceMeta;

    public Collection<ServiceMeta> getOtherServiceMeta() {
        return otherServiceMeta;
    }

    public ServiceMeta getCurServiceMeta() {
        return curServiceMeta;
    }

    /**
     * 管理服务
     * @param curServiceMeta
     * @param otherServiceMeta
     * @return
     */
    public static ServiceMetaRes build(ServiceMeta curServiceMeta, Collection<ServiceMeta> otherServiceMeta) {
        final ServiceMetaRes serviceMetaRes = new ServiceMetaRes();
        serviceMetaRes.curServiceMeta = curServiceMeta;
        if (otherServiceMeta.size() == 1) { //避免报空指针异常
            otherServiceMeta = new ArrayList<>();
        } else {
            otherServiceMeta.remove(curServiceMeta);
        }
        serviceMetaRes.otherServiceMeta = otherServiceMeta;
        return serviceMetaRes;
        
    }
    
}
