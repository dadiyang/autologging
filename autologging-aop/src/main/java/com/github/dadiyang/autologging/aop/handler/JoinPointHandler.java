package com.github.dadiyang.autologging.aop.handler;

import com.github.dadiyang.autologging.aop.aspect.JoinPointInfo;
import com.github.dadiyang.autologging.core.listener.LogTraceListener;

import java.util.List;

/**
 * 切点处理器
 *
 * @author dadiyang
 * @since 2019/3/1
 */
public interface JoinPointHandler {
    /**
     * 切点处理
     *
     * @param joinPointInfo 切点相关信息
     * @return 处理结果
     * @throws Throwable 抛出的异常
     */
    Object handle(JoinPointInfo joinPointInfo) throws Throwable;

    /**
     * 设置日志跟踪监听器
     *
     * @param logTraceListeners 监听器列表
     */
    void setLogTraceListeners(List<LogTraceListener> logTraceListeners);
}
