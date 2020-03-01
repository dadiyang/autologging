package com.github.dadiyang.autologging.aop.aspect;

import com.github.dadiyang.autologging.core.logtrace.LogTrace;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Date;

/**
 * 切点的信息
 *
 * @author dadiyang
 * @since 2019/3/1
 */
@Data
@NoArgsConstructor
public class JoinPointInfo {
    private Class<?> clazz;
    /**
     * 真实的类名称，有些被切入的类是代理类，这个属性存放真实的类名称
     */
    private String actualClassName;
    private Method method;
    private Object[] args;
    private Object result;
    private Throwable throwable;
    private String thread;
    private long timeConsume;
    private long traceId;
    private int stackDepth;
    private String aspect;

    public JoinPointInfo(JoinPoint joinPoint, long timeConsume, Object result, Throwable throwable) {
        this.clazz = joinPoint.getTarget().getClass();
        // 默认真实类名与类名一致
        this.actualClassName = clazz.getName();
        this.method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        this.args = joinPoint.getArgs();
        this.timeConsume = timeConsume;
        this.result = result;
        this.throwable = throwable;
    }

    public LogTrace toLogTrace(String appName) {
        LogTrace logTrace = new LogTrace();
        logTrace.setClassName(getActualClassName());
        logTrace.setMethod(getMethod().getName());
        logTrace.setTraceId(getTraceId());
        logTrace.setStackDepth(getStackDepth());
        logTrace.setAppName(appName);
        logTrace.setMethodTimeConsume(getTimeConsume());
        logTrace.setAspect(getAspect());
        logTrace.setException(throwable);
        logTrace.setTimestamp(new Date());
        logTrace.setThread(getThread());
        return logTrace;
    }
}
