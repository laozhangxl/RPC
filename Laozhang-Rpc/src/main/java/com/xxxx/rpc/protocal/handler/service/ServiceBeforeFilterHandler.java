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
 * 前置拦截器，请求前
 */
public class ServiceBeforeFilterHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {
    
    private Logger logger = LoggerFactory.getLogger(ServiceBeforeFilterHandler.class);
    
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol<RpcRequest> protocol) throws Exception {
        final RpcRequest request = protocol.getBody();
        //获取过滤器数据对象
        final FilterData filterData = new FilterData(request);
        RpcResponse response = new RpcResponse();
        MsgHeader header = protocol.getHeader();

        try {
            FilterConfig.getServiceBeforeFilterChain().doFilter(filterData);
        } catch (Exception e) {
            //过滤器链执行过程中发生异常
            RpcProtocol<RpcResponse> resProtocol = new RpcProtocol<>();
            //修改消息头和消息体的错误信息
            header.setStatus((byte) MsgStatus.FAILED.ordinal());
            response.setException(e);
            logger.error("before process request {} error", header.getRequestId(), e);
            //写入新的消息中返回错误信息
            resProtocol.setHeader(header);
            resProtocol.setBody(response);
            //写入并刷新通道
            channelHandlerContext.writeAndFlush(resProtocol);
            return;
        }
        //执行过程无异常，传递给下一个处理器
        channelHandlerContext.fireChannelRead(protocol);
    }
}
