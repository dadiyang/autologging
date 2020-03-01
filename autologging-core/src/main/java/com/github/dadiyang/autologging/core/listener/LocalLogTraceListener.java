package com.github.dadiyang.autologging.core.listener;

import com.github.dadiyang.autologging.core.configuration.AutoLogConfig;
import com.github.dadiyang.autologging.core.logtrace.LogTrace;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;

/**
 * 打印日志到本地的监听器
 *
 * @author dadiyang
 * @since 2019/3/1
 */
@Slf4j
public class LocalLogTraceListener implements LogTraceListener {
    private final AutoLogConfig autoLogConfig;

    public LocalLogTraceListener(AutoLogConfig autoLogConfig) {
        this.autoLogConfig = autoLogConfig;
    }

    /**
     * 将本监听器注册到 Holder 中，以提供静态获取的方法
     */
    @PostConstruct
    public void registry() {
        ListenerHolder.addListener(this);
    }

    @Override
    public void update(LogTrace logTrace) {
        if (autoLogConfig != null && autoLogConfig.getLocalConfig() != null) {
            AutoLogConfig.LocalConfig localConfig = autoLogConfig.getLocalConfig();
            if (!autoLogConfig.getLocalConfig().getEnable()) {
                return;
            }
            // 没有发生异常且方法耗时小于 localConfig 配置的阈值时不处理
            if (!logTrace.hasError()
                    && logTrace.getMethodTimeConsume() < localConfig.getTimeConsumeThreshold()) {
                return;
            }
        }
        long consumeTime = logTrace.getMethodTimeConsume();
        if (logTrace.hasError()) {
            // 如果发生异常, 打印error日志记录异常
            String msg = formErrorMsg(logTrace, consumeTime);
            log.error(msg);
        } else {
            String msg = formNormalMsg(logTrace, consumeTime);
            log.info(msg);
        }
    }

    protected String formNormalMsg(LogTrace logTrace, long consumeTime) {
        String requestInfo = getRequestInfo(logTrace);
        return String.format("%s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s",
                logTrace.getHostName(), logTrace.getAppName(), logTrace.getTraceId(), logTrace.getStackDepth(), logTrace.getAspect(),
                requestInfo, logTrace.getClassName(), logTrace.getMethod(),
                logTrace.getArgs(), logTrace.getResult(), consumeTime);
    }

    protected String formErrorMsg(LogTrace logTrace, long consumeTime) {
        String requestInfo = getRequestInfo(logTrace);
        return String.format("%s | %s | %s | %s | %s | %s| %s | %s | %s | %s | %s",
                logTrace.getHostName(), logTrace.getAppName(), logTrace.getTraceId(), logTrace.getStackDepth(), logTrace.getAspect(),
                requestInfo, logTrace.getClassName(), logTrace.getMethod(),
                logTrace.getArgs(), consumeTime, logTrace.getExceptionStackTrace());
    }

    protected String getRequestInfo(LogTrace logTrace) {
        String requestInfo = "";
        if (StringUtils.isNotBlank(logTrace.getUrl())) {
            // userPrincipal 很多情况下获取不到，因此要特殊处理
            String userMsg = StringUtils.isBlank(logTrace.getUserPrincipal())
                    ? ""
                    : logTrace.getUserPrincipal();
            requestInfo = String.format("%s %s %s%s  ", logTrace.getRequestMethod(),
                    logTrace.getRemoteIp(), logTrace.getUrl(), userMsg);
        }
        return requestInfo;
    }
}
