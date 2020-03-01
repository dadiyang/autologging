package com.github.dadiyang.autologging.test.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * 没有实际含义，仅用于测试演示
 *
 * @author dadiyang
 * @since 2020/3/1
 */
@RestController
@RequestMapping("user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * 这个方法将正确返回
     */
    @GetMapping(value = "getById", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public User getById(Long id) {
        return userService.getById(id);
    }

    /**
     * 调用这些方法会抛出异常
     */
    @GetMapping(value = "updateById", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public long updateById(User user) {
        return userService.updateById(user);
    }
}
