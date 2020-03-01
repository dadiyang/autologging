package com.github.dadiyang.autologging.aop.aspect;

import com.github.dadiyang.autologging.aop.util.ArgsUtils;
import com.github.dadiyang.autologging.core.condition.ConditionalOnClass;
import org.apache.ibatis.annotations.Mapper;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * 切所有的Mapper，该 Mapper 必须包含 @Mapper 或者 Repository 注解
 *
 * @author dadiyang
 * @since 2020/3/1
 */
@Aspect
@ConditionalOnClass(name = "org.apache.ibatis.annotations.Mapper")
public class MapperLogAspect extends AbstractCommonLogAspect {
    private static final String MAPPER = "Mapper";

    @Override
    @Pointcut("@within(org.apache.ibatis.annotations.Mapper))")
    public void pointcut() {

    }

    @Override
    public String getAspectName() {
        return MAPPER;
    }

    @Override
    protected String getActualClassName(Class<?> clazz) {
        return ArgsUtils.getActualClassName(clazz, Mapper.class);
    }
}
