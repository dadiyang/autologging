package com.github.dadiyang.autologging.core.configuration;

import com.github.dadiyang.autologging.core.util.HttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 负责定时刷新配置
 *
 * @author dadiyang
 * @since 2019/3/1
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoteConfigRunner {
    private static ScheduledThreadPoolExecutor EXECUTOR;
    private final AutoLogConfig autoLogConfig;
    @Value("${autolog.remote-config-host:}")
    private String remoteConfigHost;
    private Future<?> taskFuture;

    @PostConstruct
    public void run() {
        // 只有指明要使用远程配置才启动定时刷新配置
        if (autoLogConfig.getUseRemote()) {
            if (StringUtils.isBlank(remoteConfigHost)) {
                log.warn("开启了远程配置，但是没有配置远程服务器地址，不启用远程配置");
                return;
            }
            log.info("自动日志使用远程配置，启动定时刷新配置任务");
            EXECUTOR = new ScheduledThreadPoolExecutor(1, r -> new Thread(r, "RemoteConfigRunner"));
            taskFuture = EXECUTOR.scheduleWithFixedDelay(this::refreshConfig, 0, 10, TimeUnit.SECONDS);
        }
    }

    private void refreshConfig() {
        try {
            // 结果应该是健值对的形式
            String encodedAppName = URLEncoder.encode(autoLogConfig.getAppName(), StandardCharsets.UTF_8.toString());
            String text = HttpUtils.get(remoteConfigHost + "/autolog/getConfig?appName=" + encodedAppName);
            log.debug("开始更新 autolog 配置: " + text);
            if (StringUtils.isNotBlank(text) && !"null".equals(text)) {
                Properties properties = new Properties();
                String[] lines = text.split(";");
                for (String line : lines) {
                    if (StringUtils.isNotBlank(line)) {
                        String[] kv = line.split("=");
                        if (kv.length > 1) {
                            properties.setProperty(kv[0], kv[1]);
                        }
                    }
                }
                // 刷新配置
                autoLogConfig.initByProperties(properties);
            }
        } catch (Exception e) {
            // 为不影响业务日志，配置刷新失败只打印警告信息
            log.warn("请求远程配置发生异常: {}", e.getMessage());
        }
    }

    /**
     * 服务退出时关闭线程池
     */
    @PreDestroy
    public void destroy() {
        if (taskFuture != null && !taskFuture.isCancelled() && !taskFuture.isDone()) {
            taskFuture.cancel(true);
        }
        if (EXECUTOR != null && !EXECUTOR.isShutdown()) {
            EXECUTOR.shutdownNow();
        }
    }

}
