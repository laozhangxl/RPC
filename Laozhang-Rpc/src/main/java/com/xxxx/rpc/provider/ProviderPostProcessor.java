package com.xxxx.rpc.provider;

import com.xxxx.rpc.annotation.RpcService;
import com.xxxx.rpc.common.RpcServiceNameBuilder;
import com.xxxx.rpc.common.ServiceMeta;
import com.xxxx.rpc.config.RpcProperties;
import com.xxxx.rpc.filter.FilterConfig;
import com.xxxx.rpc.poll.ThreadPollFactory;
import com.xxxx.rpc.protocal.codec.RpcDecoder;
import com.xxxx.rpc.protocal.codec.RpcEncoder;
import com.xxxx.rpc.protocal.handler.service.RpcRequestHandler;
import com.xxxx.rpc.protocal.handler.service.ServiceAfterFilterHandler;
import com.xxxx.rpc.protocal.handler.service.ServiceBeforeFilterHandler;
import com.xxxx.rpc.protocal.serialization.SerializationFactory;
import com.xxxx.rpc.registry.RegistryFactory;
import com.xxxx.rpc.registry.RegistryService;
import com.xxxx.rpc.router.LoadBalancerFactory;
import com.xxxx.rpc.utils.PropertiesUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 服务提供方后置处理器
 */
public class ProviderPostProcessor implements BeanPostProcessor, InitializingBean, EnvironmentAware {

    private Logger logger = LoggerFactory.getLogger(ProviderPostProcessor.class);

    RpcProperties rpcProperties;

    // 此处在linux环境下改为0.0.0.0
    private static String serverAddress = "127.0.0.1";

    private final Map<String, Object> rpcServiceMap = new HashMap<>();

    /**
     * 初始化，启动服务器
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Thread t = new Thread(() -> {
            try {
                startRpcServer();
            } catch (Exception e) {
                logger.error("start rpc server error...........", e);
            }
        });
        t.setDaemon(true);
        t.start();
        SerializationFactory.init();
        RegistryFactory.init();
        LoadBalancerFactory.init();
        FilterConfig.initServiceFilter();
        //todo：线程池操作
        ThreadPollFactory.setRpcServiceMap(rpcServiceMap);
    }

    /**
     * 启动 Rpc服务器
     * @throws InterruptedException
     */
    private void startRpcServer() throws InterruptedException {
        int serverPort = rpcProperties.getPort();
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            //服务器引导类对象，添加配置
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        //初始化处理器
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new RpcEncoder())
                                    .addLast(new RpcDecoder())
                            //todo: 协议层，添加协议通道信息
                                    .addLast(new ServiceBeforeFilterHandler())
                                    .addLast(new RpcRequestHandler())
                                    .addLast(new ServiceAfterFilterHandler());
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            //绑定服务器端口和地址
            ChannelFuture channelFuture = bootstrap.bind(this.serverAddress, serverPort).sync();
            logger.info("server addr {} started on port {}", this.serverAddress, serverPort);
            //等待服务器通道关闭
            channelFuture.channel().closeFuture().sync();
            //设置钩子函数，JVM关闭时进行一些清理工作
            Runtime.getRuntime().addShutdownHook(new Thread(() ->
            {
                logger.info("ShutdownHook execute start...");
                logger.info("Netty NioEventLoopGroup shutdownGracefully...");
                logger.info("Netty NioEventLoopGroup shutdownGracefully2...");
                boss.shutdownGracefully();
                worker.shutdownGracefully();
                logger.info("ShutdownHook execute end...");
            }, "Allen-thread"));
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    /**
     * 读取配置文件
     * @param environment
     */
    @Override
    public void setEnvironment(Environment environment) {
        RpcProperties properties = RpcProperties.getInstance();
        PropertiesUtils.init(properties, environment);
        rpcProperties = properties;
        System.out.println(rpcProperties.toString());
        logger.info("读取配置文件成功...");
    }

    /**
     * 服务注册
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        final Field[] fields = bean.getClass().getDeclaredFields();
        logger.info(beanName + " field:" + Arrays.toString(fields) + "注入成功");
        Class<?> beanClass = bean.getClass();
        //找到bean上带有 @RpcService的类
        RpcService rpcService = beanClass.getAnnotation(RpcService.class);
        if (rpcService != null) {
            // 可能会有多个接口,默认选择第一个接口
            String serviceName = beanClass.getInterfaces()[0].getName();
            if (!rpcService.serviceInterface().equals(void.class)) {
                //返回值不是void类型，使用指定类型名称
                serviceName = rpcService.serviceInterface().getName();
            }
            String serviceVersion = rpcService.serviceVersion();

            try {
                //服务注册
                int servicePort = rpcProperties.getPort();
                //获取注册中心 ioc
                RegistryService registryService = RegistryFactory.get(rpcProperties.getRegisterType());
                ServiceMeta serviceMeta = new ServiceMeta();
                // 服务提供方地址
                serviceMeta.setServiceAddr("127.0.0.1");
                serviceMeta.setServicePort(servicePort);
                serviceMeta.setServiceVersion(serviceVersion);
                serviceMeta.setServiceName(serviceName);
                registryService.register(serviceMeta);
                // 缓存
                rpcServiceMap.put(RpcServiceNameBuilder.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion()), bean);
                logger.info("register server {} version {}", serviceName, serviceVersion);
            } catch (Exception e) {
                logger.error("failed to register service {}", serviceVersion, e);
            }
        }
        return bean;
    }
}
