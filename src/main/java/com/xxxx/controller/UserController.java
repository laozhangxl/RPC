package com.xxxx.controller;

import com.xxxx.framework.context.ApplicationContext;
import com.xxxx.framework.context.support.ClassPathXmlApplicationContext;
import com.xxxx.service.UserService;


public class UserController {

    public static void main(String[] args) throws Exception {
        //创建spring容器对象
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
//        BeanFactory beanFactory = new XmlBeanFactory(new ClassPathResource("applicationContext.xml"));
        //从容器对象中获取userService对象
        UserService userService = null;

        userService = applicationContext.getBean("userService", UserService.class);

        //调用userService方法进行业务逻辑处理
        userService.add();
    }

}





