package com.github.dadiyang.autologging.aop.annotation;

import java.lang.annotation.*;

/**
 * 标记类或方法不需要打印监控日志
 *
 * @author dadiyang
 * @since 2020/3/1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface IgnoreLog {
}
