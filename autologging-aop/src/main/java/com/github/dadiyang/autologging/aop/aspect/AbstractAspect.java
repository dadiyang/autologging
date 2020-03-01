package com.github.dadiyang.autologging.aop.aspect;

import com.github.dadiyang.autologging.aop.handler.JoinPointHandler;
import com.github.dadiyang.autologging.aop.util.SnowFlakeIdUtils;
import com.github.dadiyang.autologging.core.configuration.AutoLogConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 所有切面的基类，负责接管处理切点执行过程
 * <p>
 * 子类负责定义切点方法和执行后对结果进行处理
 *
 * @author dadiyang
 * @since 2020/3/1
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public abstract class AbstractAspect {
    private static final ThreadLocal<Long> ID_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<Integer> STACK_THREAD_LOCAL = new ThreadLocal<>();
    private AutoLogConfig autoLogConfig;
    private Map<String, Method> enableGetterMethods = new ConcurrentHashMap<>(8);

    /**
     * 抽象切点，由子类负责实现
     */
    @Pointcut()
    public abstract void pointcut();

    /**
     * 获取切点信息处理器
     *
     * @return 切点信息处理器
     */
    protected abstract JoinPointHandler getJoinPointHandler();

    /**
     * 接管切面方法的执行过程，不拦截打了 com.github.dadiyang.aop.autologging.annotation.IgnoreLog 注解的类和方法
     */
    @Around(value = "pointcut() && !@within(com.github.dadiyang.autologging.aop.annotation.IgnoreLog) && !@annotation(com.github.dadiyang.autologging.aop.annotation.IgnoreLog)")
    public Object globalAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        if (needIgnore()) {
            // 若切面需要被忽略，则直接执行并返回，不做后续处理
            return joinPoint.proceed();
        }
        if (ID_THREAD_LOCAL.get() == null) {
            ID_THREAD_LOCAL.set(SnowFlakeIdUtils.next());
        }
        // 模拟线程栈
        if (STACK_THREAD_LOCAL.get() == null) {
            STACK_THREAD_LOCAL.set(0);
        }
        // 方法执行时，栈深度加1
        int depth = STACK_THREAD_LOCAL.get() + 1;
        STACK_THREAD_LOCAL.set(depth);
        // 目标方法执行结果
        Object result = null;
        // 记录异常
        Throwable throwable = null;
        // 目标方法执行的时间
        long start = System.currentTimeMillis();
        try {
            result = joinPoint.proceed();
        } catch (Throwable th) {
            throwable = th;
        } finally {
            long handleJoinPointStart = System.nanoTime();
            try {
                JoinPointHandler joinPointHandler = getJoinPointHandler();
                if (joinPointHandler != null) {
                    // 只记录方法执行的时间
                    long consumeTime = System.currentTimeMillis() - start;
                    // 发生异常或者方法耗时大于或等于阈值时才处理
                    if (throwable != null || consumeTime >= autoLogConfig.getTimeConsumeThreshold()) {
                        JoinPointInfo joinPointInfo = new JoinPointInfo(joinPoint, consumeTime, result, throwable);
                        joinPointInfo.setThread(Thread.currentThread().getName());
                        joinPointInfo.setTraceId(ID_THREAD_LOCAL.get());
                        joinPointInfo.setStackDepth(depth - 1);
                        joinPointInfo.setAspect(getAspectName());
                        joinPointInfo.setActualClassName(getActualClassName(joinPoint.getTarget().getClass()));
                        result = joinPointHandler.handle(joinPointInfo);
                    }
                }
            } finally {
                // 方法执行结束后栈深度减1
                int curDepth = depth - 1;
                STACK_THREAD_LOCAL.set(curDepth);
                // 栈尝试为0表示调用链已结束，移除本线程的 ThreadLocal 变量，否则会导致内存泄漏
                if (curDepth == 0) {
                    STACK_THREAD_LOCAL.remove();
                    ID_THREAD_LOCAL.remove();
                }
                if (log.isTraceEnabled()) {
                    log.trace("autologging rt: {} ns", (System.nanoTime() - handleJoinPointStart));
                }
            }
        }
        return result;
    }

    private boolean needIgnore() {
        if (autoLogConfig == null) {
            return false;
        }
        AutoLogConfig.AspectEnableConfig aspectEnableConfig = autoLogConfig.getAspectEnable();
        // 没有配置则全部都不忽略
        if (aspectEnableConfig == null) {
            return false;
        }
        // 检当前切面是否开启
        Boolean enable = true;
        try {
            String capedAspectName = StringUtils.capitalize(getAspectName());
            Method enableGetMethod = enableGetterMethods.computeIfAbsent(capedAspectName,
                    name -> getEnableGetMethod(aspectEnableConfig, capedAspectName));
            if (enableGetMethod == null) {
                // 没有则默认不忽略
                return false;
            }
            // 是否打开
            enable = (Boolean) enableGetMethod.invoke(aspectEnableConfig);
        } catch (Exception e) {
            log.warn("查检是否开启切面发生异常: {}, {}", getAspectName(), e.getMessage());
        }
        // enable 必须明确等于 false 时才忽略
        return enable != null && !enable;
    }

    private Method getEnableGetMethod(AutoLogConfig.AspectEnableConfig aspectEnableConfig, String capedAspectName) {

        Method[] methods = aspectEnableConfig.getClass().getMethods();
        Method enableGetMethod = null;
        for (Method method : methods) {
            // 必须无参
            if (method.getParameterCount() != 0) {
                continue;
            }
            // 必须 public
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            // 返回值必须为 boolean 或其包装类
            if (method.getReturnType() != Boolean.class && method.getReturnType() != boolean.class) {
                continue;
            }
            // 因为配置项是 boolean 类型的，防止 getter 实现不一致，我们兼容 getXxx/isXxx/getIsXxx 三种写法
            if (Objects.equals(("is" + capedAspectName), method.getName())) {
                enableGetMethod = method;
                break;
            } else if (Objects.equals(("get" + capedAspectName), method.getName())) {
                enableGetMethod = method;
                break;
            } else if (Objects.equals(("getIs" + capedAspectName), method.getName())) {
                enableGetMethod = method;
                break;
            }
        }
        return enableGetMethod;
    }

    /**
     * 获取切面的名称，默认为类名，子类可以通过覆盖此方法给定切面名称
     *
     * @return 切面名称
     */
    protected String getAspectName() {
        return getClass().getSimpleName();
    }

    protected String getActualClassName(Class<?> aClass) {
        return aClass.getName();
    }

    /**
     * 获取当前 traceId
     */
    public static Long getTraceId() {
        return ID_THREAD_LOCAL.get();
    }

    /**
     * 获取当前栈深度
     */
    public static Integer getStackDepth() {
        return STACK_THREAD_LOCAL.get();
    }

    @Resource
    public void setAutoLogConfig(AutoLogConfig autoLogConfig) {
        this.autoLogConfig = autoLogConfig;
    }
}
