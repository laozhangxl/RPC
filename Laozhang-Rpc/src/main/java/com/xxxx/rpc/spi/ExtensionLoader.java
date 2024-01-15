package com.xxxx.rpc.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * SPI扩展机制
 */
public class ExtensionLoader {

    //日志
    private Logger logger = LoggerFactory.getLogger(ExtensionLoader.class);

    //系统SPI目录的前缀
    private static String SYS_EXTENSION_LOADER_DIR_PREFIX = "META-INF/rpc/";

    //用户SPI目录的前缀
    private static String DIY_EXTENSION_LOADER_DIR_PREFIX = "META-INF/rpc/";

    //前缀数组，可能用于后续拼接
    private static String[] prefixs = {SYS_EXTENSION_LOADER_DIR_PREFIX, DIY_EXTENSION_LOADER_DIR_PREFIX};

    // 缓存，存储bean定义信息 key: 定义的key value：具体类
    private static Map<String, Class> extensionClassCache = new ConcurrentHashMap<>();

    // bean 定义信息 key：接口 value：接口子类s
    private static Map<String, Map<String, Class>> extensionClassCaches = new ConcurrentHashMap<>();

    //已经实例化的bean
    private static Map<String, Object> singletonsObject = new ConcurrentHashMap<>();

    //类的单例实现，通过私有构造函数和静态块来确保只有一个实例
    private static ExtensionLoader extensionLoader;

    static {
        extensionLoader = new ExtensionLoader();
    }

    public static ExtensionLoader getInstance() {
        return extensionLoader;
    }

    private ExtensionLoader() {

    }

    /**
     * 获取bean：确保在运行时只有一个实例，并且避免重复实例化相同的对象
     * @param name
     * @return
     * @param <V>
     */
    public <V> V get(String name) {
        if (!singletonsObject.containsKey(name)) {
            try {
                //todo:缓存中找不到com.xxxx.interfaces.TestInterface$1.0,可能需要配置文件
                Class<?> aClass = extensionClassCache.get(name);
                if (aClass == null) {
                    throw new ClassNotFoundException("Class not found for name: " + name);
                }
                Object instance = aClass.newInstance();
                singletonsObject.put(name, instance);
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                logger.info("ExtensionLoader报错....get()方法");
                throw new RuntimeException(e);
            }
        }
        return (V) singletonsObject.get(name);
    }

    /**
     * 获取接口下的所有类
     * @param clazz
     * @return
     */
    public List<Object> gets(Class clazz) {
        final String name = clazz.getName();
        //查看缓存中是否存在
        if (!extensionClassCaches.containsKey(name)) {
            try {
                throw new ClassNotFoundException(clazz + "未找到");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        final Map<String, Class> stringClassMap = extensionClassCaches.get(name);
        List<Object> objects = new ArrayList<>();
        if (!stringClassMap.isEmpty()) {
            stringClassMap.forEach((k, v) -> {
                try {
                    objects.add(singletonsObject.getOrDefault(k, v.newInstance()));
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return objects;
    }

    /**
     * 根据spi机制初加载bean的信息放入map
     * @param clazz
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void loadExtension(Class clazz) throws IOException, ClassNotFoundException {
        if (clazz == null) {
            throw new IllegalArgumentException("class 没找到...loadExtension");
        }
        //获取类加载器
        ClassLoader classLoader = this.getClass().getClassLoader();
        Map<String, Class> classMap = new HashMap<>();
        // 从系统SPI以及用户SPI中找bean
        for (String prefix : prefixs) {
            String spiFilePath = prefix + clazz.getName();
            //根据指定的前缀从系统和用户的SPI配置文件中读取类名
            Enumeration<URL> enumeration = classLoader.getResources(spiFilePath);
            //遍历Url
            while (enumeration.hasMoreElements()) {
                URL url = enumeration.nextElement();
                InputStreamReader inputStreamReader = new InputStreamReader(url.openStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                //解析每一行的数据
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    String[] lineAddr = line.split("=");
                    String key = lineAddr[0];
                    String name = lineAddr[1];
                    final Class<?> aClass = Class.forName(name);
                    extensionClassCache.put(key, aClass);
                    classMap.put(key, aClass);
                    logger.info("加载bean key:{} , value:{}", key, name);
                }

            }
        }
        logger.info(clazz.getName() + "类的缓存数据加入缓存");
        System.out.println(clazz.getName() + "类的缓存数据加入缓存");
        //将当前类的缓存数据加入缓存
        extensionClassCaches.put(clazz.getName(), classMap);
        System.out.println(extensionClassCaches.toString());
    }



}
