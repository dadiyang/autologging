package com.github.dadiyang.autologging.core.serializer;

/**
 * 序列化器
 *
 * @author dadiyang
 * @since 2019/3/1
 */
public interface Serializer {
    /**
     * 将对象序列化为字符串
     *
     * @param object 对象
     * @return 字符串
     */
    String serialize(Object object);
}
