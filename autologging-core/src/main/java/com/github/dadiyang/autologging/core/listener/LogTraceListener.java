package com.github.dadiyang.autologging.core.listener;

import com.github.dadiyang.autologging.core.logtrace.LogTrace;

/**
 * 日志监听器，用于在处理完日志之后做一些额外的功能，如上报日志等
 *
 * @author dadiyang
 * @since 2020/3/1
 */
@FunctionalInterface
public interface LogTraceListener {

    /**
     * 有新的日志需要处理时会回调这个方法
     *
     * @param logTrace 日志记录
     */
    void update(LogTrace logTrace);
}
