package com.xxxx.rpc.registry;

import com.alibaba.fastjson.JSON;
import com.xxxx.rpc.common.RpcServiceNameBuilder;
import com.xxxx.rpc.common.ServiceMeta;
import com.xxxx.rpc.config.RpcProperties;
import org.springframework.util.ObjectUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @description: redis注册中心
 * 思路：
 * 使用集合保存所有服务节点信息
 * 服务启动：节点使用了redis作为注册中心后，将自身信息注册到redis当中(ttl：10秒)，并开启定时任务，ttl/2。
 * 定时任务用于检测各个节点的信息，如果发现节点的时间 < 当前时间，则将节点踢出，如果没有发现，则续签自身节点
 * 将节点踢出后，从服务注册表中找到对应key删除该节点的下的服务数据信息
 * <p>
 * ttl :10秒
 * 定时任务为ttl/2
 * 节点注册后启动心跳检测，检测服务注册的key集合，如果有服务到期，则删除,自身的服务则续签
 * 服务注册后将服务注册到redis以及保存到自身的服务注册key集合，供心跳检测
 * <p>
 * 如果有节点宕机，则其他服务会检测的，如果服务都宕机，则ttl会进行管理
 */
public class RedisRegistry implements RegistryService {

    //redis 连接池
    private JedisPool jedisPool;

    //唯一标识符
    private String UUID;

    //超时时间，以毫秒为单位
    private static final int ttl = 10 * 1000;

    //存储服务的集合
    private Set<String> serviceMap = new HashSet<>();

    //定时任务执行器
    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    /**
     * 注册当前服务,将当前服务ip，端口，时间注册到redis当中，并且开启定时任务
     * 使用集合存储服务节点信息
     */
    public RedisRegistry() {
        //获取注册中心配置
        RpcProperties properties = RpcProperties.getInstance();
        //读取配置获取主机ip..端口....
        String[] split = properties.getRegisterAddr().split(":");
        //配置jedis连接池
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        //设置最大连接数
        poolConfig.setMaxTotal(10);
        //设置最大空闲连接数
        poolConfig.setMaxIdle(5);
        //jedis连接池，ip地址，端口号
        jedisPool = new JedisPool(poolConfig, split[0], Integer.valueOf(split[1]));
        this.UUID = java.util.UUID.randomUUID().toString();
        //心跳检测器
        heartbeat();
    }

    /**
     * 进行心跳检测，定时任务
     */
    private void heartbeat() {
        int sch = 5;
        scheduledExecutorService.scheduleWithFixedDelay(() -> { //定时操作
            for (String key : serviceMap) {
                //获取所有服务节点，查询服务节点的过期时间是否 < 当前时间，如果小于则有权将其下的服务信息删除
                List<ServiceMeta> serviceMetas = listServices(key);
                Iterator<ServiceMeta> iterator = serviceMetas.iterator();
                while (iterator.hasNext()) {
                    ServiceMeta node = iterator.next();
                    //比较并删除过期服务
                    if (node.getEndTime() < new Date().getTime()) {
                        iterator.remove();
                    }
                    //没有过期，将自身续签
                    if (node.getUUID().equals(this.UUID)) {
                        node.setEndTime(node.getEndTime() + ttl / 2);
                    }
                }
                //重新加载服务
                if (!ObjectUtils.isEmpty(serviceMetas)) {
                    loadService(key, serviceMetas);
                }
            }

        }, sch, sch, TimeUnit.SECONDS);

    }

    /**
     * 查询某个服务下的所有服务节点
     * @param key
     * @return
     */
    private List<ServiceMeta> listServices(String key) {
        Jedis jedis = getJedis();
        List<String> list = jedis.lrange(key, 0, -1);
        jedis.close();
        List<ServiceMeta> serviceMetas = list.stream().map(o -> JSON.parseObject(o, ServiceMeta.class)).collect(Collectors.toList());
        return serviceMetas;
    }

    /**
     * 重新加载服务
     * @param key
     * @param serviceMetas
     */
    private void loadService(String key, List<ServiceMeta> serviceMetas) {
        //定义Lua脚本，执行redis命令
        String script = "redis.call('DEL', KEYS[1])\n" +
                "for i = 1, #ARGV do\n" +
                "   redis.call('RPUSH', KEYS[1], ARGV[i])\n" +
                "end \n" +
                "redis.call('EXPIRE', KEYS[1],KEYS[2])";
        List<String> keys = new ArrayList<String>();
        keys.add(key);
        keys.add(String.valueOf(10));
        List<String> values = serviceMetas.stream().map(o -> JSON.toJSONString(o)).collect(Collectors.toList());
        Jedis jedis = getJedis();
        jedis.eval(script, keys, values); //eval方法执行Lua脚本
        jedis.close();
    }

    //获取一个与redis连接的jedis对象
    private Jedis getJedis() {
        Jedis jedis = jedisPool.getResource();
        RpcProperties properties = RpcProperties.getInstance();
        if (!ObjectUtils.isEmpty(properties.getRegisterPsw())) {
            //密码不为空，则设置密码
            jedis.auth(properties.getRegisterPsw());
        }
        return jedis;
    }

    @Override
    public void register(ServiceMeta serviceMeta) throws Exception {
        String key = RpcServiceNameBuilder.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion());
        //如果不存在这个key（唯一标识），将其加入serviceMap
        if (!serviceMap.contains(key)) {
            serviceMap.add(key);
        }
        serviceMeta.setUUID(this.UUID);
        serviceMeta.setEndTime(new Date().getTime() + ttl);
        Jedis jedis = getJedis();
        String script = "redis.call('RPUSH', KEYS[1], ARGV[1])\n" +
                "redis.call('EXPIRE', KEYS[1], ARGV[2])";
        List<String> value = new ArrayList<>();
        value.add(JSON.toJSONString(serviceMeta));
        value.add(String.valueOf(10));
        jedis.eval(script, Collections.singletonList(key), value);
        jedis.close();


    }

    @Override
    public void unRegister(ServiceMeta serviceMeta) throws Exception {

    }

    @Override
    public List<ServiceMeta> discoveries(String serviceName) {
        return listServices(serviceName);
    }

    @Override
    public void distroy() throws IOException {

    }
}
