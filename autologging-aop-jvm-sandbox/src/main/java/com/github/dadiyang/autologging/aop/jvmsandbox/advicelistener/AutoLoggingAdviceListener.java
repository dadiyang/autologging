package com.github.dadiyang.autologging.aop.jvmsandbox.advicelistener;

import com.alibaba.jvm.sandbox.api.ProcessController;
import com.alibaba.jvm.sandbox.api.listener.ext.Advice;
import com.alibaba.jvm.sandbox.api.listener.ext.AdviceListener;
import com.github.dadiyang.autologging.aop.aspect.JoinPointInfo;
import com.github.dadiyang.autologging.aop.handler.CommonLogJoinPointHandler;
import com.github.dadiyang.autologging.aop.handler.JoinPointHandler;
import com.github.dadiyang.autologging.aop.jvmsandbox.Constant;
import com.github.dadiyang.autologging.aop.jvmsandbox.util.JoinPointInfoUtil;
import com.github.dadiyang.autologging.aop.util.ArgsUtils;
import com.github.dadiyang.autologging.aop.util.SnowFlakeIdUtils;
import com.github.dadiyang.autologging.core.listener.LogTraceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author dadiyang
 * @since 2019/3/1
 */
public class AutoLoggingAdviceListener extends AdviceListener {
    private static final Logger log = LoggerFactory.getLogger(AutoLoggingAdviceListener.class);
    private static final ThreadLocal<Long> ID_THREAD_LOCAL = new ThreadLocal<Long>();
    private static final ThreadLocal<Integer> STACK_THREAD_LOCAL = new ThreadLocal<Integer>();
    /**
     * 记录开始时间，key -> 栈深度，value -> 开始时间
     */
    private static final ThreadLocal<Map<Integer, Long>> START_TIME_THREAD_LOCAL = new ThreadLocal<Map<Integer, Long>>();
    /**
     * 缓存，不必每次都生成新的 Handler
     */
    private static final Map<ClassLoader, JoinPointHandler> HANDLER_MAP = new ConcurrentHashMap<>();
    private static final String CONSTRUCTOR = "<init>";
    private final String aspectName;
    private String[] annotationNames;
    protected List<LogTraceListener> logTraceListener;

    public AutoLoggingAdviceListener(String aspectName, List<LogTraceListener> logTraceListener, String... annotationNames) {
        this.aspectName = aspectName;
        this.annotationNames = annotationNames;
        this.logTraceListener = logTraceListener;
    }

    /**
     * 方法执行前记录堆栈和跟踪id
     */
    @Override
    protected void before(Advice advice) throws Throwable {
        if (ID_THREAD_LOCAL.get() == null) {
            ID_THREAD_LOCAL.set(SnowFlakeIdUtils.next());
        }
        // 模拟线程栈
        if (STACK_THREAD_LOCAL.get() == null) {
            STACK_THREAD_LOCAL.set(0);
        }
        // 记录开始时间
        if (START_TIME_THREAD_LOCAL.get() == null) {
            START_TIME_THREAD_LOCAL.set(new HashMap<Integer, Long>());
        }
        // 方法执行时，栈深度加1
        int depth = STACK_THREAD_LOCAL.get() + 1;
        STACK_THREAD_LOCAL.set(depth);
        START_TIME_THREAD_LOCAL.get().put(depth, System.currentTimeMillis());
    }

    @Override
    protected void afterThrowing(Advice advice) throws Throwable {
        afterAdvice(advice);
    }

    @Override
    protected void afterReturning(Advice advice) throws Throwable {
        afterAdvice(advice);
    }

    /**
     * 获取切点信息处理器
     *
     * @param classLoader 目标类加载器
     * @return 切点信息处理器
     */
    protected JoinPointHandler getJoinPointHandler(ClassLoader classLoader) {
        // 从缓存中获取
        return HANDLER_MAP.computeIfAbsent(classLoader, cl -> {
            CommonLogJoinPointHandler handler = new CommonLogJoinPointHandler(Constant.getAutoLogConfig());
            handler.setLogTraceListeners(logTraceListener);
            return handler;
        });
    }

    private void afterAdvice(Advice advice) throws Throwable {
        int depth = STACK_THREAD_LOCAL.get();
        try {
            if (CONSTRUCTOR.equals(advice.getBehavior().getName())) {
                // 不处理构造器
                return;
            }
            JoinPointHandler joinPointHandler = getJoinPointHandler(advice.getTarget().getClass().getClassLoader());
            if (joinPointHandler != null) {
                long consumeTime = System.currentTimeMillis() - START_TIME_THREAD_LOCAL.get().remove(depth);
                JoinPointInfo joinPointInfo = JoinPointInfoUtil.createJoinPointInfo(advice, consumeTime);
                joinPointInfo.setTraceId(ID_THREAD_LOCAL.get());
                joinPointInfo.setStackDepth(depth - 1);
                joinPointInfo.setAspect(aspectName);
                joinPointInfo.setActualClassName(ArgsUtils.getActualClassName(advice.getTarget().getClass(), getAnnotationNames()));
                try {
                    Object result = joinPointHandler.handle(joinPointInfo);
                    // 如果返回值被替换了，则替换为新的返回值
                    if (result != advice.getReturnObj()) {
                        ProcessController.returnImmediately(result);
                    }
                } catch (Throwable e) {
                    // 异常被替换了，则抛出被替换后的异常
                    if (e != advice.getThrowable()) {
                        ProcessController.throwsImmediately(e);
                    }
                }
            }
        } finally {
            // 方法执行结束后栈深度减1
            int curDepth = depth - 1;
            STACK_THREAD_LOCAL.set(curDepth);
            // 栈深度为0表示调用链已结束，移除本线程的 ThreadLocal 变量，否则可能会导致内存泄漏
            if (curDepth == 0) {
                STACK_THREAD_LOCAL.remove();
                ID_THREAD_LOCAL.remove();
                START_TIME_THREAD_LOCAL.remove();
            }
        }
    }

    public String[] getAnnotationNames() {
        return annotationNames;
    }

    public static void setTraceId(long traceId) {
        ID_THREAD_LOCAL.set(traceId);
    }

    public static Long getTraceId() {
        return ID_THREAD_LOCAL.get();
    }
}