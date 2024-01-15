package com.xxxx.rpc.protocal.handler.service;

import com.xxxx.rpc.common.RpcRequest;
import com.xxxx.rpc.common.RpcResponse;
import com.xxxx.rpc.common.constants.MsgStatus;
import com.xxxx.rpc.filter.FilterConfig;
import com.xxxx.rpc.filter.FilterData;
import com.xxxx.rpc.protocal.MsgHeader;
import com.xxxx.rpc.protocal.RpcProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 后置拦截器：响应后
 */
public class ServiceAfterFilterHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {
    
    private Logger logger = LoggerFactory.getLogger(ServiceAfterFilterHandler.class);
    
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol<RpcResponse> protocol) throws Exception {
        final FilterData filterData = new FilterData();
        filterData.setData(protocol.getBody());
        RpcResponse response = new RpcResponse();
        MsgHeader header = protocol.getHeader();
        try {
            FilterConfig.getServiceAfterFilterChain().doFilter(filterData);
        } catch (Exception e) {
            header.setStatus((byte) MsgStatus.FAILED.ordinal());
            response.setException(e);
            logger.error("after process request {} error", header.getRequestId(), e);
        }
        channelHandlerContext.writeAndFlush(protocol);


    }
}
