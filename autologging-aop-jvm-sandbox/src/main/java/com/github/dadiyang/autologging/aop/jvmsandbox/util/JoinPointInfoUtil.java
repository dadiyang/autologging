package com.github.dadiyang.autologging.aop.jvmsandbox.util;

import com.alibaba.jvm.sandbox.api.listener.ext.Advice;
import com.github.dadiyang.autologging.aop.aspect.JoinPointInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @author dadiyang
 * @since 2019/3/1
 */
public class JoinPointInfoUtil {
    private static final Logger log = LoggerFactory.getLogger(JoinPointInfoUtil.class);

    private JoinPointInfoUtil() {
        throw new UnsupportedOperationException("工具类不允许被初始化");
    }

    public static JoinPointInfo createJoinPointInfo(Advice joinPoint, long timeConsume) {
        JoinPointInfo joinPointInfo = new JoinPointInfo();
        joinPointInfo.setClazz(joinPoint.getTarget().getClass());
        try {
            Method method = joinPointInfo.getClazz().getMethod(joinPoint.getBehavior().getName(), joinPoint.getBehavior().getParameterTypes());
            joinPointInfo.setMethod(method);
        } catch (NoSuchMethodException e) {
            log.warn("获取目标方法失败: " + e.getMessage());
        }
        joinPointInfo.setArgs(joinPoint.getParameterArray());
        joinPointInfo.setTimeConsume(timeConsume);
        joinPointInfo.setResult(joinPoint.getReturnObj());
        joinPointInfo.setThrowable(joinPoint.getThrowable());
        return joinPointInfo;
    }

}
