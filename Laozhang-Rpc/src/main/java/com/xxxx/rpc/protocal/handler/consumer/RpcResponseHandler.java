package com.xxxx.rpc.protocal.handler.consumer;

import com.xxxx.rpc.common.RpcFuture;
import com.xxxx.rpc.common.RpcRequestHolder;
import com.xxxx.rpc.common.RpcResponse;
import com.xxxx.rpc.protocal.RpcProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 响应拦截器
 */
public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol<RpcResponse> msg) throws Exception {
        long requestId = msg.getHeader().getRequestId();
        RpcFuture<RpcResponse> future = RpcRequestHolder.REQUEST_MAP.remove(requestId);
        //设置成功的结果，通知等待响应
        future.getPromise().setSuccess(msg.getBody());
    }
}
