package com.github.dadiyang.autologging.aop.handler;

import com.github.dadiyang.autologging.aop.aspect.JoinPointInfo;

/**
 * 异常处理器
 *
 * @author dadiyang
 * @since 2020/3/1
 */
public interface ExceptionHandler {
    /**
     * 自定义异常处理
     *
     * @param joinPointInfo          切点相关信息
     * @return 方法返回值，这个返回值将会直接做为被调用方法的返回值
     * @throws Throwable 抛出的异常，此应该将视为目标方法的异常被抛出
     */
    Object handleException(JoinPointInfo joinPointInfo) throws Throwable;
}
