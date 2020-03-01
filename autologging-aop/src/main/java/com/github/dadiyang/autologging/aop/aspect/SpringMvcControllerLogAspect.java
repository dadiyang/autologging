package com.github.dadiyang.autologging.aop.aspect;

import com.github.dadiyang.autologging.core.condition.ConditionalOnClass;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * 此类负责定义用于 SpringMVC 记录 Controller 请求日志切面
 *
 * @author dadiyang
 * @since 2020/3/1
 */
@Aspect
@ConditionalOnClass(name = {"org.springframework.stereotype.Controller", "org.springframework.web.bind.annotation.RestController", "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse"})
public class SpringMvcControllerLogAspect extends AbstractControllerLogAspect {
    /**
     * 拦截所有的 Controller
     */
    @Override
    @Pointcut("@within(org.springframework.stereotype.Controller) || @within(org.springframework.web.bind.annotation.RestController)")
    public void pointcut() {
    }
}
