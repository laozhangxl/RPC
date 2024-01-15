package com.xxxx.consumer.demos.test;

import com.xxxx.interfaces.TestInterface;
import com.xxxx.rpc.annotation.RpcReference;
import com.xxxx.rpc.common.constants.FaultTolerantRules;
import com.xxxx.rpc.common.constants.LoadBalancerRules;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    //该注解用于服务发现
    @RpcReference(timeout = 10000L, faultTolerant = FaultTolerantRules.FailPass, loadBalancer = LoadBalancerRules.RoundRobin)
    TestInterface testInterface;

    @RequestMapping("/test1/{msg}")
    public String test1(@PathVariable String msg) {
        testInterface.test1(msg);
        return "test1方法执行...." + msg;
    }

    @RequestMapping("/test2/{msg}")
    public String test2(@PathVariable String msg) {
        testInterface.test2(msg);
        return "test2方法执行...." + msg;
    }

    @RequestMapping("/test3/{msg}")
    public String test3(@PathVariable String msg) {
        testInterface.test3(msg);
        return "test3方法执行...." + msg;
    }

}
