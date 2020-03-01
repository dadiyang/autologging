package com.github.dadiyang.autologging.core.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * @author dadiyang
 * @since 2019/3/1
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnMissingBeanCondition.class)
public @interface ConditionalOnMissingBean {

    Class<?>[] value() default {};
}
