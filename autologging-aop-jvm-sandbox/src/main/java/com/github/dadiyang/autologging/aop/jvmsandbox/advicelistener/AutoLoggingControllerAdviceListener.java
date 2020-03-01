package com.github.dadiyang.autologging.aop.jvmsandbox.advicelistener;

import com.github.dadiyang.autologging.aop.handler.ControllerLogJoinPointHandler;
import com.github.dadiyang.autologging.aop.handler.JoinPointHandler;
import com.github.dadiyang.autologging.aop.jvmsandbox.Constant;
import com.github.dadiyang.autologging.aop.jvmsandbox.ContextHolderRequestContextProvider;
import com.github.dadiyang.autologging.core.listener.LogTraceListener;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.util.List;
import java.util.Map;

/**
 * Controller层的自动日志通知监听器
 *
 * @author dadiyang
 * @since 2019/3/1
 */
public class AutoLoggingControllerAdviceListener extends AutoLoggingAdviceListener {
    /**
     * 使用软引用的 map 做缓存，不必每次都生成新的 Handler
     */
    private static final Map<ClassLoader, ControllerLogJoinPointHandler> HANDLER_MAP = new ConcurrentReferenceHashMap<>();

    public AutoLoggingControllerAdviceListener(List<LogTraceListener> logTraceListeners, String... annotationNames) {
        super("Controller", logTraceListeners, annotationNames);
    }

    /**
     * 获取切点信息处理器
     *
     * @param classLoader 目标类加载器
     * @return 切点信息处理器
     */
    @Override
    protected JoinPointHandler getJoinPointHandler(ClassLoader classLoader) {
        // 从缓存中获取
        return HANDLER_MAP.computeIfAbsent(classLoader, cl -> {
            ControllerLogJoinPointHandler handler = new ControllerLogJoinPointHandler(Constant.getAutoLogConfig(), new ContextHolderRequestContextProvider());
            handler.setLogTraceListeners(logTraceListener);
            return handler;
        });
    }

}
