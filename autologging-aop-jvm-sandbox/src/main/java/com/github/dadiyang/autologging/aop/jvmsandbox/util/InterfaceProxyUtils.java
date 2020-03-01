package com.github.dadiyang.autologging.aop.jvmsandbox.util;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 接口代理工具类
 *
 * @author sandbox-debug-module
 */
public class InterfaceProxyUtils {
    /**
     * 构造一个接口的实现傀儡类，用接口去调用目标类
     *
     * @param interfaceClass 目标接口
     * @param target         傀儡类实例
     * @param <T>            目标接口类型
     * @return 被目标接口操纵的傀儡对象实例
     */
    public static <T> T puppet(final Class<T> interfaceClass,
                               final Object target) {
        //noinspection unchecked
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new WrapInvocationHandler() {

                    @Override
                    public Object invoke(Object proxy, Method interfaceMethod, Object[] args) throws Throwable {
                        Method method = getTargetMethod(interfaceMethod, target);
                        method.setAccessible(true);
                        return method.invoke(target, args);
                    }

                }
        );
    }

    /**
     * 用于包装目标对象操作的代理方法处理
     */
    private static abstract class WrapInvocationHandler implements InvocationHandler {
        final Map<Method, Method> mappingOfWrapMethods = new ConcurrentHashMap<>();

        /**
         * 比较interfaceMethod和targetMethod两个方法是否接近
         *
         * @param interfaceMethod 接口声明的方法
         * @param targetMethod    目标对象声明的方法
         * @return TRUE:接近;FALSE:不接近
         */
        boolean isCloseTo(final Method interfaceMethod, final Method targetMethod) {
            return StringUtils.equals(interfaceMethod.getName(), targetMethod.getName())
                    && Arrays.deepEquals(getJavaClassNameArray(interfaceMethod.getParameterTypes()),
                    getJavaClassNameArray(targetMethod.getParameterTypes()));
        }

        Method getTargetMethod(final Method interfaceMethod, final Object target) throws NoSuchMethodException {
            if (mappingOfWrapMethods.containsKey(interfaceMethod)) {
                return mappingOfWrapMethods.get(interfaceMethod);
            }
            for (final Method targetMethod : target.getClass().getMethods()) {
                if (isCloseTo(interfaceMethod, targetMethod)) {
                    mappingOfWrapMethods.put(interfaceMethod, targetMethod);
                    return targetMethod;
                }
            }
            throw new NoSuchMethodException(String.format("%s.%s(%s) method not found!",
                    getJavaClassName(target.getClass()),
                    interfaceMethod.getName(),
                    StringUtils.join(getJavaClassNameArray(interfaceMethod.getParameterTypes()), ",")
            ));
        }
    }

    private static String[] getJavaClassNameArray(final Class<?>[] classArray) {
        if (classArray == null || classArray.length == 0) {
            return null;
        }
        final String[] javaClassNameArray = new String[classArray.length];
        for (int index = 0; index < classArray.length; index++) {
            Class<?> clazz = classArray[index];
            javaClassNameArray[index] = getJavaClassName(clazz);
        }
        return javaClassNameArray;
    }

    private static String getJavaClassName(Class<?> clazz) {
        return clazz.isArray()
                ? clazz.getCanonicalName()
                : clazz.getName();
    }
}
