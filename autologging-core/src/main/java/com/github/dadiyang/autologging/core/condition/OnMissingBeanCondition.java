package com.github.dadiyang.autologging.core.condition;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 * 检查某个 Bean 是否不存在，以决定某个 Bean 是否加载
 *
 * @author dadiyang
 * @since 2019/3/1
 */
public class OnMissingBeanCondition implements Condition {
    private static final String VALUE = "value";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attr = metadata.getAnnotationAttributes(ConditionalOnMissingBean.class.getName());
        Object obj = attr.get(VALUE);
        if (obj instanceof Class<?>[]) {
            Class<?>[] clazz = (Class<?>[]) obj;
            for (Class<?> aClass : clazz) {
                try {
                    context.getBeanFactory().getBean(aClass);
                } catch (NoSuchBeanDefinitionException e) {
                    // Bean 不存在，则返回 true
                    return true;
                } catch (BeansException ignored) {
                    // 其他异常不管
                }
            }
        }
        return false;
    }
}
