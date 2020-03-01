package com.github.dadiyang.autologging.core.annotation;

import com.github.dadiyang.autologging.core.serializer.FastJsonSerializer;
import com.github.dadiyang.autologging.core.serializer.GsonJsonSerializer;
import com.github.dadiyang.autologging.core.serializer.JacksonJsonSerializer;
import com.github.dadiyang.autologging.core.serializer.ToStringSerializer;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 注册序列化器
 *
 * @author dadiyang
 * @since 2019/3/1
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({FastJsonSerializer.class, JacksonJsonSerializer.class, GsonJsonSerializer.class, ToStringSerializer.class})
public @interface EnableSerializer {
}
