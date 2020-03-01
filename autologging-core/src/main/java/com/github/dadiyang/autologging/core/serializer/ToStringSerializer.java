package com.github.dadiyang.autologging.core.serializer;

import com.github.dadiyang.autologging.core.condition.ConditionalOnMissingBean;

import java.util.Arrays;
import java.util.Objects;

/**
 * 调用对象的 toString 方法的序列化器
 * <p>
 * 此序列化器是默认兜底的，即当没有任何其他序列化器可用时，才会使用
 *
 * @author dadiyang
 * @since 2019/3/1
 */
@ConditionalOnMissingBean(Serializer.class)
public class ToStringSerializer implements Serializer {
    @Override
    public String serialize(Object object) {
        if (object == null) {
            return "";
        }
        if (object.getClass().isArray()) {
            if (object instanceof Object[]) {
                return Arrays.deepToString((Object[]) object);
            } else if (object instanceof byte[]) {
                return Arrays.toString((byte[]) object);
            } else if (object instanceof short[]) {
                return Arrays.toString((short[]) object);
            } else if (object instanceof int[]) {
                return Arrays.toString((int[]) object);
            } else if (object instanceof long[]) {
                return Arrays.toString((long[]) object);
            } else if (object instanceof float[]) {
                return Arrays.toString((float[]) object);
            } else if (object instanceof double[]) {
                return Arrays.toString((double[]) object);
            } else if (object instanceof char[]) {
                return Arrays.toString((char[]) object);
            } else if (object instanceof boolean[]) {
                return Arrays.toString((boolean[]) object);
            }
        }
        return Objects.toString(object);
    }
}
