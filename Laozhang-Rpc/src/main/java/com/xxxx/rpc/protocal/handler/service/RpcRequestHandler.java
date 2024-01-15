package com.xxxx.rpc.protocal.handler.service;

import com.xxxx.rpc.common.RpcRequest;
import com.xxxx.rpc.poll.ThreadPollFactory;
import com.xxxx.rpc.protocal.RpcProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 处理调用方发送数据，调用方法
 */
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {

    public RpcRequestHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol<RpcRequest> rpcRequestRpcProtocol) throws Exception {
        
        //todo: 利用线程池进行调用
        ThreadPollFactory.submitRequest(channelHandlerContext, rpcRequestRpcProtocol);
        
    }
}
