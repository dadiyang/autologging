package com.github.dadiyang.autologging.test.user;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author dadiyang
 * @since 2020/3/1
 */
@Data
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserDAO userDAO;

    @Override
    public User getById(Long id) {
        return userDAO.getById(id);
    }

    @Override
    public long updateById(User user) {
        return userDAO.updateById(user);
    }
}
