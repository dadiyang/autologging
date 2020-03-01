package com.github.dadiyang.autologging.test.user;

import org.springframework.stereotype.Repository;

/**
 * @author dadiyang
 * @since 2020/3/1
 */
@Repository
public class UserFakeDAOImpl implements UserDAO {
    @Override
    public User getById(Long id) {
        return new User(id, "张三");
    }

    @Override
    public long updateById(User user) {
        throw new UnsupportedOperationException("模拟抛出异常");
    }
}
