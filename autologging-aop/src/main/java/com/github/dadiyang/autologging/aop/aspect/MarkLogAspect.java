package com.github.dadiyang.autologging.aop.aspect;

import com.github.dadiyang.autologging.aop.annotation.MarkLog;
import com.github.dadiyang.autologging.aop.util.ArgsUtils;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * 切所有的包含 @Log 的类
 *
 * @author dadiyang
 * @since 2019/3/1
 */
@Aspect
public class MarkLogAspect extends AbstractCommonLogAspect {
    private static final String MARK_LOG = "MarkLog";

    @Override
    @Pointcut("@within(com.github.dadiyang.autologging.aop.annotation.EnableMarkLog) || @annotation(com.github.dadiyang.autologging.aop.annotation.EnableMarkLog)")
    public void pointcut() {

    }

    @Override
    public String getAspectName() {
        return MARK_LOG;
    }

    @Override
    protected String getActualClassName(Class<?> clazz) {
        return ArgsUtils.getActualClassName(clazz, MarkLog.class);
    }
}
