package com.github.dadiyang.autologging.aop.aspect;

import com.github.dadiyang.autologging.aop.handler.ControllerLogJoinPointHandler;
import com.github.dadiyang.autologging.aop.handler.JoinPointHandler;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 记录请求日志的基类, 负责处理 Controller 方法的执行之后记录请求的详细信息及统一异常处理逻辑
 *
 * @author dadiyang
 * @since 2020/3/1
 */
@Aspect
@Slf4j
@Component
public abstract class AbstractControllerLogAspect extends AbstractAspect {
    private static final String CONTROLLER = "Controller";
    private ControllerLogJoinPointHandler controllerLogJoinPointHandler;

    @Override
    protected JoinPointHandler getJoinPointHandler() {
        return controllerLogJoinPointHandler;
    }

    @Autowired
    public void setControllerLogJoinPointHandler(ControllerLogJoinPointHandler controllerLogJoinPointHandler) {
        this.controllerLogJoinPointHandler = controllerLogJoinPointHandler;
    }

    @Override
    protected String getAspectName() {
        return CONTROLLER;
    }
}
