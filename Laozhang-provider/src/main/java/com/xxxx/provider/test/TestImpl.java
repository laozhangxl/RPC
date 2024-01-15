package com.xxxx.provider.test;

import com.xxxx.interfaces.TestInterface;
import com.xxxx.rpc.annotation.RpcService;

@RpcService
public class TestImpl implements TestInterface {

    @Override
    public void test1(String msg) {
        System.out.println("TestImpl === test1...." + msg);
    }

    @Override
    public void test2(String msg) {
        System.out.println("TestImpl === test1...." + msg);
    }

    @Override
    public void test3(String msg) {
        System.out.println("TestImpl === test1...." + msg);
    }
}
