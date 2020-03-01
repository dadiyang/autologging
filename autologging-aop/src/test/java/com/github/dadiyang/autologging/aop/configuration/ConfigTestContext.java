package com.github.dadiyang.autologging.aop.configuration;

import com.github.dadiyang.autologging.aop.serializer.SerializerWrapper;
import com.github.dadiyang.autologging.core.configuration.AutoLogConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Import({AutoLogConfig.class, SerializerWrapper.class})
@PropertySource("classpath:application.properties")
public class ConfigTestContext {
}
