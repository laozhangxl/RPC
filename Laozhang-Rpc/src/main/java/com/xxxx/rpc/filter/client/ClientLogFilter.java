package com.xxxx.rpc.filter.client;

import com.xxxx.rpc.filter.ClientBeforeFilter;
import com.xxxx.rpc.filter.FilterData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 输出日志信息
 */
public class ClientLogFilter implements ClientBeforeFilter {
    
    private Logger logger = LoggerFactory.getLogger(ClientLogFilter.class);
    
    @Override
    public void doFilter(FilterData filterData) {
        logger.info(filterData.toString());
    }
}
