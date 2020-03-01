package com.github.dadiyang.autologging.core.configuration;

/**
 * 通过静态属性持有 appName 配置项
 *
 * @author dadiyang
 * @since 2019/3/1
 */
public class AppNameHolder {
    private static String appName;

    public static String getAppName() {
        return appName;
    }

    public static void setAppName(String appName) {
        AppNameHolder.appName = appName;
    }
}
