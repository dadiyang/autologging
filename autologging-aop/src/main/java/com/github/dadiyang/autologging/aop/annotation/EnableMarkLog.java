package com.github.dadiyang.autologging.aop.annotation;

import com.github.dadiyang.autologging.aop.aspect.MarkLogAspect;
import com.github.dadiyang.autologging.aop.handler.CommonLogJoinPointHandler;
import com.github.dadiyang.autologging.core.annotation.EnableSerializer;
import com.github.dadiyang.autologging.core.configuration.AutoLogConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启 @Log 注解对应的日志切面
 *
 * @author dadiyang
 * @since 2019/3/1
 */
@Documented
@EnableSerializer
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Import({AutoLogConfig.class, CommonLogJoinPointHandler.class, MarkLogAspect.class})
public @interface EnableMarkLog {
}
