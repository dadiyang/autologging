package com.github.dadiyang.autologging.aop.jvmsandbox;

import com.github.dadiyang.autologging.core.configuration.AutoLogConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class Constant {
    private static Properties properties;
    public static final String ANNOTATION_REST_CONTROLLER = "org.springframework.web.bind.annotation.RestController";
    public static final String ANNOTATION_CONTROLLER = "org.springframework.stereotype.Controller";
    public static final String ANNOTATION_RS_PATH = "javax.ws.rs.Path";

    public static final String ANNOTATION_SERVICE = "org.springframework.stereotype.Service";
    public static final String ASPECT_SERVICE = "Service";

    public static final String ANNOTATION_MAPPER = "org.apache.ibatis.annotations.Mapper";
    public static final String ASPECT_MAPPER = "Mapper";

    public static final String ANNOTATION_REPOSITORY = "org.springframework.stereotype.Repository";
    public static final String ASPECT_REPOSITORY = "Repository";

    public static final String ANNOTATION_HTTP_API = "com.github.dadiyang.httpinvoker.annotation.HttpApi";
    public static final String ASPECT_HTTP_API = "HttpApi";

    public static final String ANNOTATION_LOG = "com.github.dadiyang.autologging.aop.annotation.MarkLog";
    public static final String ASPECT_LOG = "MarkLog";

    private static AutoLogConfig autoLogConfig;

    /**
     * 通过给定的 inputStream 进行初始化
     *
     * @param in 配置文件输入流
     */
    public static void init(InputStream in) throws IOException {
        properties = new Properties();
        properties.load(in);
        init();
    }

    private static void init() {
        String bootstrapServer = Constant.get("autolog.kafka.bootstrapServer");
        String clientId = Constant.get("autolog.kafka.clientId");
        boolean kafkaEnable = Boolean.parseBoolean(Constant.get("autolog.kafka.enable"));
        String kafkaTopic = Constant.get("autolog.kafka.topic");
        String appName = Constant.get("autolog.appName");
        autoLogConfig = new AutoLogConfig();
        autoLogConfig.setKafka(new AutoLogConfig.KafkaConfig(kafkaEnable, bootstrapServer, clientId, kafkaTopic));
        autoLogConfig.setLocalConfig(new AutoLogConfig.LocalConfig());
        autoLogConfig.setAspectEnable(new AutoLogConfig.AspectEnableConfig());
        autoLogConfig.setSerialize(new AutoLogConfig.SerializeConfig());
        autoLogConfig.setAppName(appName);
    }

    /**
     * 通过给定 map 进行初始化
     *
     * @param map 配置项
     */
    public static void init(Map<String, String> map) {
        properties = new Properties();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            properties.put(entry.getKey(), entry.getValue());
        }
        init();
    }

    public static AutoLogConfig getAutoLogConfig() {
        return autoLogConfig;
    }

    public static void setAutoLogConfig(AutoLogConfig autoLogConfig) {
        Constant.autoLogConfig = autoLogConfig;
    }

    public static String get(String key) {
        return properties == null ? "" : properties.getProperty(key);
    }
}
