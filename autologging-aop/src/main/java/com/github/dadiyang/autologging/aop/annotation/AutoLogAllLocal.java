package com.github.dadiyang.autologging.aop.annotation;

import com.github.dadiyang.autologging.core.listener.LocalLogTraceListener;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 同时开启所有切面，只打印到本地，不上报日志
 *
 * @author dadiyang
 * @since 2020/3/1
 */
@Documented
@EnableMarkLog
@EnableControllerLog
@EnableServiceLog
@EnableMapperLog
@EnableHttpApiInvokerLog
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(LocalLogTraceListener.class)
public @interface AutoLogAllLocal {
}
