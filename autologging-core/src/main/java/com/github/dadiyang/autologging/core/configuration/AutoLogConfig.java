package com.github.dadiyang.autologging.core.configuration;

import com.github.dadiyang.autologging.core.util.ReflectionUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * 统一管理整个模块的配置项
 * <p>
 * 刷新配置通过 @Value 注解进行反射解析
 * <p>
 * 因为需要通过刷新配置，所以必须只能打 @Component 注解不能使用 @Configuration，关于其区别请查阅Spring官方文档 1.12.1 小节
 * <p>
 * 因为有一个线程专门定时刷新配置项，为保证高并发情况下线程间的可见性，所有的属性都添加了 volatile 关键字
 * <p>
 * 此配置类需考虑非 Spring 集成的场景，因此都显式赋了初值
 *
 * @author dadiyang
 * @since 2019/3/1
 */
@Data
@Component
@NoArgsConstructor
@Import(RemoteConfigRunner.class)
public class AutoLogConfig {

    @Value("${autolog.app-name}")
    private volatile String appName;
    /**
     * 是否使用远程配置
     */
    @Value("${autolog.use-remote:false}")
    private volatile Boolean useRemote = false;
    /**
     * 耗时阈值，只有当方法耗时大于这个值时才处理，单位毫秒
     */
    @Value("${autolog.time-consume-threshold:-1}")
    private volatile Long timeConsumeThreshold = -1L;

    private volatile LocalConfig localConfig = new LocalConfig();

    private volatile SerializeConfig serialize = new SerializeConfig();

    private volatile AspectEnableConfig aspectEnable = new AspectEnableConfig();

    private volatile KafkaConfig kafka = new KafkaConfig();

    /**
     * 通过 properties 配置进行初始化或刷新配置
     */
    public void initByProperties(Properties properties) {
        ReflectionUtils.fillByProperties(this, properties, false);
    }

    @Autowired
    public void setSerialize(SerializeConfig serialize) {
        this.serialize = serialize;
    }

    @Autowired
    public void setAspectEnable(AspectEnableConfig aspectEnable) {
        this.aspectEnable = aspectEnable;
    }

    @Autowired
    public void setKafka(KafkaConfig kafka) {
        this.kafka = kafka;
    }

    @Autowired
    public void setLocalConfig(LocalConfig localConfig) {
        this.localConfig = localConfig;
    }

    @Data
    @Component
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LocalConfig {
        @Value("${autolog.local.enable:true}")
        private volatile Boolean enable = true;
        /**
         * 耗时阈值，只有当方法耗时大于这个值时打印到本地，单位毫秒
         */
        @Value("${autolog.local.time-consume-threshold:-1}")
        private volatile Long timeConsumeThreshold = -1L;
    }

    @Data
    @Component
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SerializeConfig {
        @Value("${autolog.serialize.args-full:true}")
        private volatile Boolean argsFull = true;
        @Value("${autolog.serialize.args-max-length:512}")
        private volatile Integer argsMaxLength = 512;
        @Value("${autolog.serialize.result-full:false}")
        protected volatile Boolean resultFull = false;
        @Value("${autolog.serialize.result-max-length:512}")
        private volatile Integer resultMaxLength = 512;
    }

    @Data
    @Component
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AspectEnableConfig {
        @Value("${autolog.aspect-enable.mapper:true}")
        private volatile Boolean mapper = true;
        @Value("${autolog.aspect-enable.repository:true}")
        private volatile Boolean repository = true;
        @Value("${autolog.aspect-enable.service:true}")
        private volatile Boolean service = true;
        @Value("${autolog.aspect-enable.service-contract:true}")
        private volatile Boolean serviceContract = true;
        @Value("${autolog.aspect-enable.markLog:true}")
        private volatile Boolean markLog = true;
        @Value("${autolog.aspect-enable.http-api:true}")
        private volatile Boolean httpApi = true;
        @Value("${autolog.aspect-enable.controller:true}")
        private volatile Boolean controller = true;
    }

    @Data
    @Component
    @NoArgsConstructor
    public static class KafkaConfig {
        /**
         * 是否启用 kafka 上报
         */
        @Value("${autolog.kafka.enable:false}")
        private volatile Boolean enable = false;

        // 这些配置如无特殊情况，请保持默认即可

        @Value("${autolog.kafka.batch-size:16384}")
        private volatile Integer batchSize = 16384;
        @Value("${autolog.kafka.acks:all}")
        private volatile String acks = "all";
        @Value("${autolog.kafka.buffer-memory:33554432}")
        private volatile Integer bufferMemory = 33554432;
        @Value("${autolog.kafka.metadata-max-age-ms:300000}")
        private volatile Integer metadataMaxAgeMs = 300000;
        @Value("${autolog.kafka.max-block-ms:500}")
        private volatile Integer maxBlockMs = 500;
        @Value("${autolog.kafka.request-timeout-ms:30000}")
        private volatile Integer requestTimeoutMs = 30000;
        @Value("${autolog.kafka.key-serializer:org.apache.kafka.common.serialization.StringSerializer}")
        private volatile String keySerializer = "org.apache.kafka.common.serialization.StringSerializer";
        @Value("${autolog.kafka.value-serializer:org.apache.kafka.common.serialization.StringSerializer}")
        private volatile String valueSerializer = "org.apache.kafka.common.serialization.StringSerializer";

        // 下面是必须自定义的配置

        @Value("${autolog.kafka.bootstrap-server:}")
        private volatile String bootstrapServer;
        @Value("${autolog.kafka.client-id:}")
        private volatile String clientId;
        @Value("${autolog.kafka.topic:}")
        private volatile String topic;

        public KafkaConfig(Boolean enable, String bootstrapServer, String clientId, String topic) {
            this.enable = enable;
            this.bootstrapServer = bootstrapServer;
            this.clientId = clientId;
            this.topic = topic;
        }
    }
}
