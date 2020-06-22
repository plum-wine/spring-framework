package com.github.entity;

import org.springframework.beans.factory.FactoryBean;

/**
 * @author hangs.zhang
 * @date 2020/06/21 14:17
 * *****************
 * function:
 */
public class UserFactoryBean implements FactoryBean<User> {

    @Override
    public User getObject() throws Exception {
        return new User();
    }

    @Override
    public Class<?> getObjectType() {
        return User.class;
    }
}
