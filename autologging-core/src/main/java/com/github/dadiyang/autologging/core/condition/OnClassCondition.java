package com.github.dadiyang.autologging.core.condition;

import com.github.dadiyang.autologging.core.util.ReflectionUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 * 检查某个类是否存在，以决定某个 Bean 是否加载
 *
 * @author dadiyang
 * @since 2019/3/1
 */
public class OnClassCondition implements Condition {
    private static final String NAME = "name";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> className = metadata.getAnnotationAttributes(ConditionalOnClass.class.getName());
        Object obj = className.get(NAME);
        if (obj instanceof String[]) {
            String[] names = (String[]) obj;
            for (String name : names) {
                if (!ReflectionUtils.classExists(name)) {
                    return false;
                }
            }
        }
        return true;
    }
}
