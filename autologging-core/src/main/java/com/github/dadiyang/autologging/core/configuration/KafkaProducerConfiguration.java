package com.github.dadiyang.autologging.core.configuration;

import com.github.dadiyang.autologging.core.condition.ConditionalOnClass;
import com.github.dadiyang.autologging.core.condition.ConditionalOnProperty;
import com.github.dadiyang.autologging.core.listener.KafkaLogTraceListener;
import com.github.dadiyang.autologging.core.serializer.JsonSerializer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * kafka 生产者配置
 *
 * @author dadiyang
 * @since 2020/3/1
 */
@Setter
@Getter
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(value = "autolog.kafka.enable", havingValue = "true")
@ConditionalOnClass(name = "org.apache.kafka.clients.producer.KafkaProducer")
public class KafkaProducerConfiguration {
    private final AutoLogConfig.KafkaConfig kafkaConfig;
    private String enable;
    private KafkaProducer<String, String> kafkaProducer;

    @Bean
    public KafkaProducer<String, String> kafkaProducer() {
        if (!kafkaConfig.getEnable()) {
            return null;
        }
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaConfig.getBootstrapServer());
        props.put("client.id", kafkaConfig.getClientId());
        props.put("key.serializer", kafkaConfig.getKeySerializer());
        props.put("value.serializer", kafkaConfig.getValueSerializer());
        props.put("batch.size", kafkaConfig.getBatchSize());
        props.put("acks", kafkaConfig.getAcks());
        props.put("buffer.memory", kafkaConfig.getBufferMemory());
        props.put("metadata.max.age.ms", kafkaConfig.getMetadataMaxAgeMs());
        props.put("max.block.ms", kafkaConfig.getMaxBlockMs());
        props.put("request.timeout.ms", kafkaConfig.getRequestTimeoutMs());
        log.info("enable kafka log service with clientId: " + kafkaConfig.getClientId() + ", kafkTopic: " + kafkaConfig.getClientId());
        kafkaProducer = new KafkaProducer<>(props);
        addShutdownHook(kafkaProducer);
        return kafkaProducer;
    }

    @Bean
    public KafkaLogTraceListener kafkaLogTraceListener(JsonSerializer jsonSerializer) {
        return new KafkaLogTraceListener(kafkaProducer(), kafkaConfig, jsonSerializer);
    }

    /**
     * 在 jvm 退出时尝试关闭 kafka
     */
    private void addShutdownHook(KafkaProducer<?, ?> kafkaProducer) {
        if (kafkaProducer == null) {
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                kafkaProducer.close(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("无法关闭 kafka", e);
            }
        }));
    }
}
