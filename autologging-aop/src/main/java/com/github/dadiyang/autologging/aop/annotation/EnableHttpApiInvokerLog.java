package com.github.dadiyang.autologging.aop.annotation;

import com.github.dadiyang.autologging.aop.aspect.HttpApiInvokerLogAspect;
import com.github.dadiyang.autologging.aop.handler.CommonLogJoinPointHandler;
import com.github.dadiyang.autologging.core.annotation.EnableSerializer;
import com.github.dadiyang.autologging.core.configuration.AutoLogConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启基于 http-api-invoker 框架的接口日志
 *
 * @author dadiyang
 * @since 2020/3/1
 */
@Documented
@EnableSerializer
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({CommonLogJoinPointHandler.class, HttpApiInvokerLogAspect.class, AutoLogConfig.class})
public @interface EnableHttpApiInvokerLog {
}
