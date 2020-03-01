package com.github.dadiyang.autologging.aop.aspect;

import com.github.dadiyang.autologging.aop.handler.JoinPointHandler;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 负责接管普通的切点, 子类负责定义切点
 *
 * @author dadiyang
 * @since 2020/3/1
 */
@Aspect
@Component
public abstract class AbstractCommonLogAspect extends AbstractAspect {
    private JoinPointHandler commonLogJoinPointHandler;

    @Override
    protected JoinPointHandler getJoinPointHandler() {
        return commonLogJoinPointHandler;
    }

    @Autowired
    @Qualifier("CommonLogJoinPointHandler")
    public void setCommonLogJoinPointHandler(JoinPointHandler commonLogJoinPointHandler) {
        this.commonLogJoinPointHandler = commonLogJoinPointHandler;
    }
}
