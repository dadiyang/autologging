package com.github.dadiyang.autologging.aop.annotation;

import com.github.dadiyang.autologging.aop.aspect.MapperLogAspect;
import com.github.dadiyang.autologging.aop.handler.CommonLogJoinPointHandler;
import com.github.dadiyang.autologging.core.annotation.EnableSerializer;
import com.github.dadiyang.autologging.core.configuration.AutoLogConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启 Mapper 切面
 *
 * @author dadiyang
 * @since 2020/3/1
 */
@Documented
@EnableSerializer
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({AutoLogConfig.class, CommonLogJoinPointHandler.class, MapperLogAspect.class})
public @interface EnableMapperLog {
}
