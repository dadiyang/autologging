package com.github.dadiyang.autologging.core.logtrace;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.net.InetAddress;
import java.util.Date;

/**
 * 日志上报实体
 *
 * @author dadiyang
 * @since 2020/3/1
 */
@Data
@Slf4j
@NoArgsConstructor
public class LogTrace {
    private static final String HOST_NAME;

    static {
        String hostName = "";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            log.warn("无法获取到当前机器的hostName");
        }
        HOST_NAME = hostName;
    }

    /**
     * 用于追踪调用链
     */
    private long traceId;
    /**
     * 方法调用深度，入口为 0
     */
    private int stackDepth;
    /**
     * 应用的名称
     */
    private String appName;
    /**
     * 切面名称
     */
    private String aspect;
    /**
     * 请求的链接，只在 Controller 切面中有
     */
    private String url;
    /**
     * 请求方法：GET/POST/PUT/DELETE 等
     */
    private String requestMethod;
    /**
     * 用户凭证，只在 Controller 切面中有
     */
    private String userPrincipal;
    /**
     * 调用者ip，需要在 nginx 中添加:
     * <pre>
     * proxy_set_header Host $host;
     * proxy_set_header X-Real-IP $remote_addr;
     * proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
     * </pre>
     */
    private String remoteIp;
    /**
     * 机器的 hostName
     */
    private String hostName = HOST_NAME;
    /**
     * 当前执行的方法所在的类全类名
     */
    private String className;
    /**
     * 当前执行的方法
     */
    private String method;
    /**
     * 方法调用参数
     */
    private String args;
    /**
     * 方法调用结果
     */
    private String result;
    /**
     * 异常信息
     */
    private String exceptionStackTrace;
    /**
     * 方法执行耗时
     */
    private long methodTimeConsume;
    /**
     * 线程名
     */
    private String thread;
    /**
     * 时间戳
     */
    private Date timestamp;
    /**
     * 异常
     */
    @Getter(AccessLevel.NONE)
    private Throwable exception;

    public String getRequestMethod() {
        return StringUtils.upperCase(requestMethod);
    }

    /**
     * 获取异常堆栈信息，使用延迟初始化和缓存的方式提升性能
     */
    public String getExceptionStackTrace() {
        if (exception == null) {
            return null;
        }
        if (exceptionStackTrace != null) {
            return exceptionStackTrace;
        } else {
            return ExceptionUtils.getStackTrace(exception);
        }
    }

    public boolean hasError() {
        return exception != null;
    }

    /**
     * 设置异常，并清理之前的异常堆栈
     */
    public void setException(Throwable exception) {
        this.exception = exception;
        exceptionStackTrace = null;
    }
}
