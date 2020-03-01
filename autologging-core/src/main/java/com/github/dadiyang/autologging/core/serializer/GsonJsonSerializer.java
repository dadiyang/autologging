package com.github.dadiyang.autologging.core.serializer;

import com.github.dadiyang.autologging.core.condition.ConditionalOnClass;
import com.github.dadiyang.autologging.core.condition.ConditionalOnMissingBean;
import com.google.gson.Gson;

/**
 * 基于 gson 的 json 序列化器，仅在类路径中有 Gson 并且没有注册其他的 json 序列化器时使用
 *
 * @author dadiyang
 * @since 2019/3/1
 */
@ConditionalOnMissingBean(JsonSerializer.class)
@ConditionalOnClass(name = "com.google.gson.Gson")
public class GsonJsonSerializer implements JsonSerializer {
    private static final Gson GSON = new Gson();

    @Override
    public String serialize(Object object) {
        return GSON.toJson(object);
    }
}
