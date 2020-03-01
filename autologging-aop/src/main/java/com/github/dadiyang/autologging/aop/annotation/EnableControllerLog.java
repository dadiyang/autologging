package com.github.dadiyang.autologging.aop.annotation;

import com.github.dadiyang.autologging.aop.aspect.SpringMvcControllerLogAspect;
import com.github.dadiyang.autologging.aop.handler.ControllerLogJoinPointHandler;
import com.github.dadiyang.autologging.aop.handler.DefaultRequestContextProvider;
import com.github.dadiyang.autologging.core.annotation.EnableSerializer;
import com.github.dadiyang.autologging.core.configuration.AutoLogConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启基于 Jersey 框架的请求日志
 *
 * @author dadiyang
 * @since 2020/3/1
 */
@Documented
@EnableSerializer
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({AutoLogConfig.class, DefaultRequestContextProvider.class,
        ControllerLogJoinPointHandler.class, SpringMvcControllerLogAspect.class})
public @interface EnableControllerLog {
}
