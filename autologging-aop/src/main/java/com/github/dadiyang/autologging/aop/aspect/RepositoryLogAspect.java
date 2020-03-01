package com.github.dadiyang.autologging.aop.aspect;

import com.github.dadiyang.autologging.aop.util.ArgsUtils;
import com.github.dadiyang.autologging.core.condition.ConditionalOnClass;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Repository;

/**
 * 切所有的包含 @Repository 的类
 *
 * @author dadiyang
 * @since 2020/3/1
 */
@Aspect
@ConditionalOnClass(name = "org.springframework.stereotype.Repository")
public class RepositoryLogAspect extends AbstractCommonLogAspect {
    private static final String REPOSITORY = "Repository";

    @Override
    @Pointcut("@within(org.springframework.stereotype.Repository))")
    public void pointcut() {

    }

    @Override
    public String getAspectName() {
        return REPOSITORY;
    }

    @Override
    protected String getActualClassName(Class<?> clazz) {
        return ArgsUtils.getActualClassName(clazz, Repository.class);
    }

}
