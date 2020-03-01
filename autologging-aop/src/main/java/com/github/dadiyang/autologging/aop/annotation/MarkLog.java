package com.github.dadiyang.autologging.aop.annotation;

import com.github.dadiyang.autologging.core.annotation.EnableSerializer;

import java.lang.annotation.*;

/**
 * 打上此注解的方法或类中的所有方法，都会成为自动日志的切点，即对其开启自动日志功能
 *
 * @author dadiyang
 * @since 2019/3/1
 */
@Documented
@EnableSerializer
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface MarkLog {
}
