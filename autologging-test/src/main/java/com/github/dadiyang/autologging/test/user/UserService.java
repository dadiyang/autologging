package com.github.dadiyang.autologging.test.user;

/**
 * @author dadiyang
 * @since 2020/3/1
 */
public interface UserService {
    User getById(Long id);

    long updateById(User user);
}
