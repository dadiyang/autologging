package com.github.dadiyang.autologging.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 反射工具类
 *
 * @author dadiyang
 * @since 2019/3/1
 */
@Slf4j
public class ReflectionUtils {
    private static Set<Class<?>> SIMPLE_TYPES = new HashSet<>(Arrays.asList(
            Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Character.class,
            Boolean.class, String.class, Enum.class, Date.class,
            byte.class, short.class, int.class, long.class, float.class, double.class, char.class, boolean.class));
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^/]+?)}");
    private static Map<String, Boolean> existCache = new ConcurrentHashMap<String, Boolean>();

    private ReflectionUtils() {
        throw new UnsupportedOperationException("静态工具类不允许被实例化");
    }

    /**
     * 通过给定的 properties 给带有 @Value 注解的对象属性赋值
     *
     * @param obj        目标对象
     * @param properties 配置
     * @param useDefault 如果配置中没有，是否使用 @Value 注解设置的默认值
     */
    public static void fillByProperties(Object obj, Properties properties, boolean useDefault) {
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            // 排除静态属性
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            Class<?> fieldType = field.getType();
            // 如果不是简单类型则递归
            if (!SIMPLE_TYPES.contains(fieldType)) {
                try {
                    Object fieldVal = field.get(obj);
                    if (fieldVal != null) {
                        fillByProperties(field.get(obj), properties, useDefault);
                    }
                } catch (IllegalAccessException e) {
                    log.error("递归反射赋值发生异常", e);
                }
                continue;
            }
            // 排除没有 Value 注解的字段
            if (!field.isAnnotationPresent(Value.class)) {
                continue;
            }
            Value val = field.getAnnotation(Value.class);
            String valuePlaceHolder = val.value();
            // 正则获取 Value 占位符的 key
            Matcher matcher = VARIABLE_PATTERN.matcher(valuePlaceHolder);
            while (matcher.find()) {
                String key = matcher.group(1);
                // 默认值是以 : 分隔的
                String[] keyPart = key.split(":");
                String newValStr;
                if (useDefault) {
                    newValStr = properties.getProperty(keyPart[0], keyPart.length > 1 ? keyPart[1] : null);
                } else {
                    newValStr = properties.getProperty(keyPart[0]);
                }
                if (newValStr != null) {
                    // 将字符串转换为字段的类型
                    Object newVal = convertToFieldType(fieldType, newValStr);
                    try {
                        Object fieldVal = field.get(obj);
                        if (newVal != null && !Objects.equals(newVal, fieldVal)) {
                            String methodName = "set" + StringUtils.capitalize(field.getName());
                            Method setter = obj.getClass().getMethod(methodName, fieldType);
                            if (setter != null) {
                                setter.invoke(obj, newVal);
                            }
                            log.info("field value change, from {} to {}, prop key: {}", fieldVal, newVal, key);
                        }
                    } catch (Exception e) {
                        log.error("通过反射赋值发生异常", e);
                    }
                }
            }
        }
    }

    private static Object convertToFieldType(Class<?> fieldType, String newValStr) {
        Object newVal = null;
        if (String.class == fieldType) {
            newVal = newValStr;
        } else if (Boolean.class.isAssignableFrom(fieldType)) {
            newVal = Boolean.parseBoolean(newValStr);
        } else if (Short.class.isAssignableFrom(fieldType)) {
            newVal = Short.parseShort(newValStr);
        } else if (Integer.class.isAssignableFrom(fieldType)) {
            newVal = Integer.parseInt(newValStr);
        } else if (Long.class.isAssignableFrom(fieldType)) {
            newVal = Long.parseLong(newValStr);
        } else if (Float.class.isAssignableFrom(fieldType)) {
            newVal = Float.parseFloat(newValStr);
        } else if (Double.class.isAssignableFrom(fieldType)) {
            newVal = Double.parseDouble(newValStr);
        } else if (Character.class.isAssignableFrom(fieldType)) {
            char[] chars = newValStr.toCharArray();
            newVal = chars.length > 0 ? chars[0] : Character.UNASSIGNED;
        } else if (Date.class.isAssignableFrom(fieldType)) {
            // 日期只支持最简单的格式
            newVal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(newValStr);
        }
        return newVal;
    }

    /**
     * 检查某个全类名是否存在于 classpath 中
     */
    public static boolean classExists(String clzFullName) {
        if (StringUtils.isBlank(clzFullName)) {
            return false;
        }
        Boolean rs = existCache.get(clzFullName);
        if (rs != null && rs) {
            return true;
        }
        try {
            Class<?> clz = Class.forName(clzFullName);
            existCache.put(clzFullName, clz != null);
            return clz != null;
        } catch (ClassNotFoundException e) {
            existCache.put(clzFullName, false);
            return false;
        }
    }

    public static boolean isSimpleType(Class<?> clazz) {
        if (clazz == null) {
            return true;
        }
        return SIMPLE_TYPES.contains(clazz);
    }
}
