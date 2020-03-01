package com.github.dadiyang.autologging.aop.serializer;

import com.github.dadiyang.autologging.core.configuration.AutoLogConfig;
import com.github.dadiyang.autologging.core.serializer.JsonSerializer;
import com.github.dadiyang.autologging.core.serializer.Serializer;
import com.github.dadiyang.autologging.core.util.ReflectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.util.*;

/**
 * 序列化器的包装类，用于在序列化之前做一些通用的处理
 * <p>
 * 注意：Import 序列化器类的顺序就是它们加载的优先级，如 fast, jackson, gson, toString 这样的顺序，那么前面的 bean 被注册了，后面的就不会加载
 *
 * @author dadiyang
 * @since 2019/3/1
 */
@Slf4j
@Setter
@RequiredArgsConstructor
public class SerializerWrapper {
    private final Serializer serializer;
    private final AutoLogConfig.SerializeConfig serializeConfig;
    /**
     * 要被排除的包
     */
    private Set<String> excludePackage = new HashSet<>(Arrays.asList(
            "java.io",
            "javax.servlet",
            "org.apache.catalina",
            "org.springframework.web.servlet",
            "org.springframework.validation",
            "org.springframework.http",
            "com.github.dadiyang.httpinvoker.requestor"));

    private Set<String> includePackage = new HashSet<>(Arrays.asList(
            "java.lang",
            "java.util",
            "java.time",
            "java.math"));
    /**
     * 要被排除的类全类名
     */
    private Set<String> excludeType = new HashSet<>(Arrays.asList(
            "java.io.InputStream",
            "java.io.OutputStream"));
    /**
     * 要被包含的类全类名，在这里配置的类无需排除
     */
    private Set<String> includeType = new HashSet<>(Arrays.asList(
            "java.io.Serializable",
            "java.io.Closeable"));
    private static final Map<Class<?>, Boolean> NEED_EXCLUDE_CACHE = new ConcurrentReferenceHashMap<>(64);

    /**
     * 将入参打印成字符串
     *
     * @param fullMsg 是否只返回全部，否则返回摘要，摘要即只返回前 maxArgsLength 个字符
     */
    public String argsToString(Object[] args, boolean fullMsg) {
        if (ArrayUtils.isEmpty(args)) {
            return "";
        }
        if (serializer == null) {
            log.debug("没有可用的序列化器，使用 toString 方法");
            return Arrays.toString(args);
        }
        try {
            Object[] objects = new Object[args.length];
            boolean isJsonSerializer = serializer instanceof JsonSerializer;
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (isJsonSerializer && needExclude(arg)) {
                    objects[i] = "unsupported: " + Objects.toString(arg, "");
                } else {
                    objects[i] = arg;
                }
            }
            return fullMsg ? serializer.serialize(objects) : summary(objects, serializeConfig.getArgsMaxLength());
        } catch (Exception e) {
            log.error("序列化参数发生异常: {}", args, e);
        }
        return Arrays.toString(args);
    }

    /**
     * 判断是否需要被排除
     */
    private boolean needExclude(Object obj) {
        if (obj == null) {
            return false;
        }
        Class<?> clazz = obj.getClass();
        // 简单类型不排除
        if (ReflectionUtils.isSimpleType(clazz)) {
            return false;
        }
        if (includeType.contains(clazz.getName())) {
            return false;
        }
        return NEED_EXCLUDE_CACHE.computeIfAbsent(clazz, this::hasExcludeSuperClass);
    }

    private boolean hasExcludeSuperClass(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        if (needExclude(clazz)) {
            return true;
        }
        // 检查所实现的接口及其父类
        for (Class<?> interfaceClazz : clazz.getInterfaces()) {
            if (hasExcludeSuperClass(interfaceClazz)) {
                return true;
            }
        }
        return hasExcludeSuperClass(clazz.getSuperclass());
    }

    private boolean needExclude(Class<?> cl) {
        // 数组类型没有包
        if (cl.getPackage() == null) {
            return false;
        }
        if (includeType.contains(cl.getName())) {
            return false;
        }
        if (hasStartWith(cl, includePackage)) {
            return false;
        }
        // 任何以需要排除的包及其子包都需要排除
        return hasStartWith(cl, excludePackage) || excludeType.contains(cl.getName());
    }

    private boolean hasStartWith(Class<?> cl, Set<String> includePackage) {
        return !includePackage.isEmpty() && includePackage.stream().anyMatch(e -> e.startsWith(cl.getPackage().getName()));
    }

    /**
     * 将返回值进行序列化
     */
    public String resultToString(Object obj, boolean fullMsg) {
        if (obj == null) {
            return "";
        }
        try {
            if (needExclude(obj)) {
                return Objects.toString(obj);
            }
            return fullMsg ? serializer.serialize(obj) : summary(obj, serializeConfig.getResultMaxLength());
        } catch (Exception e) {
            log.error("序列化对象发生异常: " + e.getMessage());
        }
        return Objects.toString(obj);
    }

    /**
     * 将对象序列化后截断
     *
     * @param obj       需要被序列化的类
     * @param maxLength 最大长度
     * @return 截断后的结果
     */
    private String summary(Object obj, Integer maxLength) {
        if (obj == null || maxLength == null || maxLength == 0) {
            return "";
        }
        try {
            String argsString = serializer.serialize(obj);
            // 负数不做截断
            if (maxLength < 0) {
                return argsString;
            }
            if (argsString == null) {
                return "";
            }
            if (argsString.length() > maxLength) {
                // 参数的简单摘要
                argsString = argsString.substring(0, maxLength) + "...";
            }
            return argsString;
        } catch (Exception e) {
            log.warn("将对象序列化并截断出错: {}", e.getMessage());
        }
        return obj.toString();
    }
}
