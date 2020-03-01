package com.github.dadiyang.autologging.core.util;

import com.github.dadiyang.autologging.core.configuration.AutoLogConfig;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.*;

public class ReflectionUtilsTest {

    @Test
    public void fillByProperties() {
        AutoLogConfig autoLogConfig = new AutoLogConfig();
        Properties properties = new Properties();
        try (InputStream in = ReflectionUtilsTest.class.getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ReflectionUtils.fillByProperties(autoLogConfig, properties, false);
        System.out.println(autoLogConfig);
        assertEquals(properties.getProperty("autolog.app-name"), autoLogConfig.getAppName());
    }
}