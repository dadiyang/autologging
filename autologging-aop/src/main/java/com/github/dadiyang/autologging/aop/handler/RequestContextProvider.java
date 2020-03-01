package com.github.dadiyang.autologging.aop.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 请求上下文提供器
 *
 * @author dadiyang
 * @since 2019/3/1
 */
public interface RequestContextProvider {
    /**
     * 获取当前请求对象
     *
     * @return 当前请求
     */
    HttpServletRequest getRequest();

    /**
     * 获取当前响应
     *
     * @return 当前响应
     */
    HttpServletResponse getResponse();

    /**
     * 获取当前登录人用户名
     *
     * @return 当前登录人用户名
     */
    String getUsername();
}
