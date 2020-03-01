package com.github.dadiyang.autologging.core.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;
import java.util.Objects;

/**
 * 检查某个属性是否等于指定的值，决定某个 Bean 是否需要加载
 *
 * @author dadiyang
 * @since 2019/3/1
 */
public class OnPropertyCondition implements Condition {
    private static final String HAVING_VALUE = "havingValue";
    private static final String VALUE = "value";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attr = metadata.getAnnotationAttributes(ConditionalOnProperty.class.getName());
        Object obj = attr.get(VALUE);
        String value = null;
        if (attr.containsKey(HAVING_VALUE)) {
            value = (String) attr.get(HAVING_VALUE);
        }
        if (obj instanceof String[]) {
            String[] names = (String[]) obj;
            for (String name : names) {
                String prop = context.getEnvironment().getProperty(name);
                if (value != null && prop == null) {
                    return false;
                } else if (!Objects.equals(value, prop)) {
                    return false;
                }
            }
        }
        return true;
    }
}
