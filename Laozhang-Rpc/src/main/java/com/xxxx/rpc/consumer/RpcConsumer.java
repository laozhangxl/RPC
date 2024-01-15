package com.xxxx.rpc.consumer;


import com.xxxx.rpc.common.RpcRequest;
import com.xxxx.rpc.common.ServiceMeta;
import com.xxxx.rpc.protocal.RpcProtocol;
import com.xxxx.rpc.protocal.codec.RpcDecoder;
import com.xxxx.rpc.protocal.codec.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消费方发送数据，初始化netty客户端
 */
public class RpcConsumer {

    //配置和启动客户端
    private final Bootstrap bootstrap;

    //处理客户端数据，接受连接，处理数据
    private final EventLoopGroup eventLoopGroup;

    //记录日志
    private Logger logger = LoggerFactory.getLogger(RpcConsumer.class);

    public RpcConsumer() {
        //成员变量初始化
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(4);
        //.channel()：指定通道类型，这是Netty 中处理基于 NIO 的客户端的通道类型
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true) //保持连接活跃
                .handler(new ChannelInitializer<SocketChannel>() { //设置处理器
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        //todo: 协议，编码解码
                        //处理rpc请求和响应的解码和编码
                        socketChannel.pipeline()
                                .addLast(new RpcEncoder())
                                .addLast(new RpcDecoder())
                                .addLast();
                    }
                });
    }

    /**
     * 发送请求
     * @param protocol 消息
     * @param serviceMeta 服务
     */
    public void sendRequest(RpcProtocol<RpcRequest> protocol, ServiceMeta serviceMeta) throws Exception {
        if (serviceMeta != null) {
            //说明未连接，进行连接，连接RPC服务，同步操作，会一直阻塞
            ChannelFuture future = bootstrap.connect(serviceMeta.getServiceAddr(), serviceMeta.getServicePort()).sync();
            //添加监听器
            future.addListener(arg0 -> {
                if (future.isSuccess()) {
                    logger.info("连接 rpc server {} 端口 {} 成功.", serviceMeta.getServiceAddr(), serviceMeta.getServicePort());
                } else {
                    logger.info("连接 rpc server {} 端口 {} 成功.", serviceMeta.getServiceAddr(), serviceMeta.getServicePort());
                    future.cause().printStackTrace();
                    eventLoopGroup.shutdownGracefully();
                }
            });
            //写入数据
            future.channel().writeAndFlush(protocol);
        }
    }
}
