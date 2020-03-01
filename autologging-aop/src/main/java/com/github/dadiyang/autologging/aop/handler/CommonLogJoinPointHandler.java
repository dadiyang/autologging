package com.github.dadiyang.autologging.aop.handler;

import com.github.dadiyang.autologging.aop.aspect.JoinPointInfo;
import com.github.dadiyang.autologging.aop.serializer.SerializerWrapper;
import com.github.dadiyang.autologging.core.configuration.AutoLogConfig;
import com.github.dadiyang.autologging.core.listener.LocalLogTraceListener;
import com.github.dadiyang.autologging.core.listener.LogTraceListener;
import com.github.dadiyang.autologging.core.logtrace.LogTrace;
import com.github.dadiyang.autologging.core.serializer.FastJsonSerializer;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * 负责处理普通的切点(非Controller)
 *
 * @author dadiyang
 * @since 2019/3/1
 */
@Slf4j
@Setter
@Import(SerializerWrapper.class)
@Component("CommonLogJoinPointHandler")
public class CommonLogJoinPointHandler implements JoinPointHandler {
    private ExceptionHandler exceptionHandler;
    protected List<LogTraceListener> logTraceListeners;
    protected SerializerWrapper serializerWrapper;
    protected AutoLogConfig autoLogConfig;

    public CommonLogJoinPointHandler(AutoLogConfig autoLogConfig) {
        this.autoLogConfig = autoLogConfig;
        // 如果上下文中没有提供日志对象处理器，则添加默认的
        logTraceListeners = new LinkedList<>();
        logTraceListeners.add(new LocalLogTraceListener(autoLogConfig));
        serializerWrapper = new SerializerWrapper(new FastJsonSerializer(), autoLogConfig.getSerialize());
    }

    @Autowired
    public CommonLogJoinPointHandler(ApplicationContext applicationContext) {
        serializerWrapper = applicationContext.getBean(SerializerWrapper.class);
        autoLogConfig = applicationContext.getBean(AutoLogConfig.class);
        Map<String, LogTraceListener> listenerMap = applicationContext.getBeansOfType(LogTraceListener.class);
        if (listenerMap != null && !listenerMap.isEmpty()) {
            logTraceListeners = new LinkedList<>(listenerMap.values());
        } else {
            // 如果上下文中没有提供日志对象处理器，则添加默认的
            logTraceListeners = new LinkedList<>();
            logTraceListeners.add(new LocalLogTraceListener(autoLogConfig));
        }
        try {
            // 异常处理器是可选的
            exceptionHandler = applicationContext.getBean(ExceptionHandler.class);
        } catch (NoSuchBeanDefinitionException e) {
            log.debug("no exceptioin handler provided");
        }
    }

    @Override
    public Object handle(JoinPointInfo joinPointInfo) throws Throwable {
        Object result = joinPointInfo.getResult();
        // 只在有注册监听器时才生成 logTrace 对象
        if (logTraceListeners != null && !logTraceListeners.isEmpty()) {
            LogTrace logTrace = getLogTrace(joinPointInfo);
            postMsg(logTrace);
        }
        Throwable throwable = joinPointInfo.getThrowable();
        if (throwable != null) {
            return handleException(joinPointInfo);
        } else {
            return result;
        }
    }

    protected LogTrace getLogTrace(JoinPointInfo joinPointInfo) {
        LogTrace logTrace = joinPointInfo.toLogTrace(autoLogConfig.getAppName());
        // 如果开启全参数或者抛出异常，则获取参数全文
        AutoLogConfig.SerializeConfig serializeConfig = autoLogConfig.getSerialize();
        // 如果配置使用全参数或者抛出了异常则使用全参数
        boolean af = serializeConfig == null || serializeConfig.getArgsFull() || joinPointInfo.getThrowable() != null;
        logTrace.setArgs(serializerWrapper.argsToString(joinPointInfo.getArgs(), af));
        // 配置明确指定为 true，才使用全返回值
        boolean rf = serializeConfig != null && serializeConfig.getResultFull();
        logTrace.setResult(serializerWrapper.resultToString(joinPointInfo.getResult(), rf));
        return logTrace;
    }

    /**
     * 处理异常，默认不处理，子类可以覆盖此方法进行统一异常处理
     * <p>
     * 此方法的执行结果将会成为目标方法的结果，无论抛出异常还是正常返回
     *
     * @return 期望目标方法返回的值
     * @throws Throwable 目标方法抛出的异常
     */
    protected Object handleException(JoinPointInfo joinPointInfo) throws Throwable {
        if (exceptionHandler != null) {
            return exceptionHandler.handleException(joinPointInfo);
        } else {
            // 没注册异常处理器则不处理异常
            throw joinPointInfo.getThrowable();
        }
    }

    /**
     * 通知监听器上报消息
     */
    protected void postMsg(LogTrace logTrace) {
        try {
            // 有监听器，则回调
            for (LogTraceListener listener : logTraceListeners) {
                listener.update(logTrace);
            }
        } catch (Throwable e) {
            // 捕获所有异常，以免影响正常业务代码
            log.warn("上报日志发生异常: {}", e.getMessage());
        }
    }
}
