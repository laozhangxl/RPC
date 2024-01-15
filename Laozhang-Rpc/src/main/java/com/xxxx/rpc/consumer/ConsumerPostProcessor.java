package com.xxxx.rpc.consumer;

import com.xxxx.rpc.annotation.RpcReference;
import com.xxxx.rpc.config.RpcProperties;
import com.xxxx.rpc.filter.FilterConfig;
import com.xxxx.rpc.protocal.serialization.SerializationFactory;
import com.xxxx.rpc.registry.RegistryFactory;
import com.xxxx.rpc.router.LoadBalancerFactory;
import com.xxxx.rpc.utils.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;


/**
 * 消费方后置处理器
 */
@Configuration
public class ConsumerPostProcessor implements BeanPostProcessor, EnvironmentAware, InitializingBean {

    //日志
    final private Logger logger = LoggerFactory.getLogger(ConsumerPostProcessor.class);

    RpcProperties rpcProperties;

    /**
     * 初始化bean
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        SerializationFactory.init();
        RegistryFactory.init();
        LoadBalancerFactory.init();
        FilterConfig.initClientFilter();
    }

    /**
     * 从配置文件中读取文件
     *
     * @param environment
     */
    @Override
    public void setEnvironment(Environment environment) {
        //读取配置文件
        RpcProperties properties = RpcProperties.getInstance();
        System.out.println(properties.toString());
        //将读取出的文件赋值
        PropertiesUtils.init(properties, environment);
        rpcProperties = properties;
        logger.info("读取配置文件成功");
    }

    /**
     * 代理层注入
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //获取所有字段
        final Field[] fields = bean.getClass().getDeclaredFields();
        // 遍历所有字段找到 @RpcReference 的字段
        for (Field field : fields) {
            if (field.isAnnotationPresent(RpcReference.class)) {
                //获取注解信息
                final RpcReference rpcReference = field.getAnnotation(RpcReference.class);
                //获取字段类型
                final Class<?> aClass = field.getType();
                //字段设置为可访问
                field.setAccessible(true);
                Object object = null;
                try {
                    //创建代理对象
                    object = Proxy.newProxyInstance(
                            aClass.getClassLoader(),
                            new Class<?>[]{aClass},
                            new RpcInvokerProxy(rpcReference.serviceVersion(), rpcReference.timeout(), rpcReference.loadBalancer(),
                                    rpcReference.faultTolerant(), rpcReference.retryCount())
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    //将代理对象设置给字段
                    field.set(bean, object);
                    field.setAccessible(false);
                    logger.info(beanName + " field:" + field.getName() + "注入成功");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    logger.info(beanName + " field:" + field.getName() + "注入失败");
                }

            }
        }
        return bean;
    }
}
