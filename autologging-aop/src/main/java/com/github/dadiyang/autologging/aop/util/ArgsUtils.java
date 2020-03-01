package com.github.dadiyang.autologging.aop.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * 参数处理工具类
 *
 * @author dadiyang
 * @since 2020/3/1
 */
@Slf4j
public class ArgsUtils {
    private ArgsUtils() {
        throw new IllegalStateException("ArgsUtils不允许实例化");
    }

    /**
     * 获取类上具有指定注解的接口的名称，如果有多个，则以第一个为准
     * <p>
     * 找不到符合条件的接口则返回 clazz 类的名称
     *
     * @param clazz      类
     * @param annotation 指定注解
     * @return 类上具有指定注解的接口的名称
     */
    public static String getActualClassName(Class<?> clazz, Class<? extends Annotation> annotation) {
        int len = clazz.getInterfaces().length;
        // 如果没实现任何接口，则为当前类名
        if (len <= 0) {
            return clazz.getName();
        }
        // 如果类有实现接口则找到打了指定注解的接口
        int ind = -1;
        for (int i = 0; i < len; i++) {
            if (clazz.getInterfaces()[i].isAnnotationPresent(annotation)) {
                ind = i;
                break;
            }
        }
        if (ind >= 0) {
            // 获取该接口的类型名做为当前类名
            return clazz.getGenericInterfaces()[ind].getTypeName();
        }
        // 所有接口都没有打指定注解，则为当前类名
        return clazz.getName();
    }

    /**
     * 获取类上具有指定注解名称的接口的名称，如果有多个，则以第一个为准
     * <p>
     * 找不到符合条件的接口则返回 clazz 类的名称
     *
     * @param clazz           类
     * @param annotationNames 指定注解全名
     * @return 类上具有指定注解的接口的名称
     */
    public static String getActualClassName(Class<?> clazz, String... annotationNames) {
        int len = clazz.getInterfaces().length;
        // 如果没实现任何接口，则为当前类名
        if (len <= 0) {
            return clazz.getName();
        }
        // 如果类有实现接口则找到打了指定注解的接口
        int ind = -1;
        for (int i = 0; i < len; i++) {
            Class<?> interfaceClass = clazz.getInterfaces()[i];
            for (Annotation annotation : interfaceClass.getAnnotations()) {
                for (String name : annotationNames) {
                    if (Objects.equals(name, annotation.annotationType().getName())) {
                        ind = i;
                        break;
                    }
                }
            }
        }
        if (ind >= 0) {
            // 获取该接口的类型名做为当前类名
            return clazz.getGenericInterfaces()[ind].getTypeName();
        }
        // 所有接口都没有打指定注解，则为当前类名
        return clazz.getName();
    }
}
