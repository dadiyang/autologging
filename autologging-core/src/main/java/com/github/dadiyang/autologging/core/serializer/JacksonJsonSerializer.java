package com.github.dadiyang.autologging.core.serializer;

import com.github.dadiyang.autologging.core.condition.ConditionalOnClass;
import com.github.dadiyang.autologging.core.condition.ConditionalOnMissingBean;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * 基于 Jackson 的序列化器，仅在类路径中有 jackson 并且没有注册其他的 json 序列化器时使用
 *
 * @author dadiyang
 * @since 2019/3/1
 */
@Slf4j
@ConditionalOnClass(name = "com.fasterxml.jackson.databind.ObjectMapper")
@ConditionalOnMissingBean(JsonSerializer.class)
public class JacksonJsonSerializer implements JsonSerializer {
    private static final ObjectMapper JACKSON_MAPPER = new ObjectMapper();

    @Override
    public String serialize(Object object) {
        if (object == null) {
            return "";
        }
        try {
            return JACKSON_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.warn("对象序列化失败: {}", object);
        }
        return "";
    }

}
