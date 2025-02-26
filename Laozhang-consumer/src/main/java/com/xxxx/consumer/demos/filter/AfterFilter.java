package com.xxxx.consumer.demos.filter;

import com.xxxx.rpc.filter.ClientAfterFilter;
import com.xxxx.rpc.filter.FilterData;


public class AfterFilter implements ClientAfterFilter {

    @Override
    public void doFilter(FilterData filterData) {
        System.out.println("客户端后置处理器启动咯");
    }
    
}
