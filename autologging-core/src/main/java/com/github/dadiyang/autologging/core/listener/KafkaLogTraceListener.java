package com.github.dadiyang.autologging.core.listener;

import com.github.dadiyang.autologging.core.configuration.AutoLogConfig;
import com.github.dadiyang.autologging.core.logtrace.LogTrace;
import com.github.dadiyang.autologging.core.serializer.JsonSerializer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.annotation.PostConstruct;

/**
 * kafka 上报日志监听器，监听到日志事件时进行日志上报
 *
 * @author dadiyang
 * @since 2020/3/1
 */
@Getter
@RequiredArgsConstructor
public class KafkaLogTraceListener implements LogTraceListener {
    private final KafkaProducer<String, String> kafkaProducer;
    private final AutoLogConfig.KafkaConfig kafkaConfig;
    private final JsonSerializer jsonSerializer;

    /**
     * 将本监听器注册到 Holder 中，以提供静态获取的方法
     */
    @PostConstruct
    public void registry() {
        ListenerHolder.addListener(this);
    }

    @Override
    public void update(LogTrace logTrace) {
        if (kafkaConfig == null || !kafkaConfig.getEnable()) {
            return;
        }
        // kafka 生产者和主题都不为空的时候才发送消息
        if (kafkaProducer != null && StringUtils.isNotBlank(kafkaConfig.getTopic())) {
            kafkaProducer.send(new ProducerRecord<>(kafkaConfig.getTopic(), jsonSerializer.serialize(logTrace)));
        }
    }
}
