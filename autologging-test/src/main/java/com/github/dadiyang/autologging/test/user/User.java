package com.github.dadiyang.autologging.test.user;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author dadiyang
 * @since 2020/3/1
 */
@Data
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
}
