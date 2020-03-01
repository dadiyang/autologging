package com.github.dadiyang.autologging.aop.annotation;

import com.github.dadiyang.autologging.core.annotation.EnableKafkaLog;

import java.lang.annotation.*;

/**
 * 同时开启所有切面并且启用上报通过 kafka 上报日志
 *
 * @author dadiyang
 * @since 2020/3/1
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@AutoLogAllLocal
@EnableKafkaLog
public @interface AutoLogAll {
}
