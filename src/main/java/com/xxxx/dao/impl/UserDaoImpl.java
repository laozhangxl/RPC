package com.xxxx.dao.impl;

import com.xxxx.dao.UserDao;

/**
 * 数据访问层实现类
 */
public class UserDaoImpl implements UserDao {

    private String username;
    private String password;

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserDaoImpl() {
        System.out.println("UserDao构造器.....");
    }

    // 这里进行了依赖注入
    @Override
    public void add() {
        System.out.println("UserDao....." + username + "==" + password);
    }
}
