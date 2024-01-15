package com.xxxx.service.impl;

import com.xxxx.dao.UserDao;
import com.xxxx.service.UserService;

/**
 * 业务逻辑层接口实现类
 */
public class UserServiceImpl implements UserService {

    private UserDao userDao;

    public UserServiceImpl() {
        System.out.println("UserService构造器......");
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void add() {
        System.out.println("UserService......");
        userDao.add();
    }
}
