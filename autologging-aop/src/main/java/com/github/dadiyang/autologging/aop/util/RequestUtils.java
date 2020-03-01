package com.github.dadiyang.autologging.aop.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

/**
 * 请求处理工具类
 *
 * @author dadiyang
 * @since 2019/3/1
 */
public class RequestUtils {
    private static final String X_FORWARDED_FOR = "X-forwarded-for";
    private static final String PROXY_CLIENT_IP = "Proxy-Client-IP";
    private static final String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
    private static final String HTTP_CLIENT_IP = "HTTP_CLIENT_IP";
    private static final String UNKNOWN = "unknown";
    private static final String HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR";
    private static final String COMMA = ",";

    /**
     * 从 request 中获取请求来源真实地址
     */
    public static String getIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String ip = request.getHeader(X_FORWARDED_FOR);
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader(PROXY_CLIENT_IP);
        }
        if (ip == null || ip.isEmpty() || RequestUtils.UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader(WL_PROXY_CLIENT_IP);
        }
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader(HTTP_CLIENT_IP);
        }
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader(HTTP_X_FORWARDED_FOR);
        }
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(COMMA)) {
            // 对于通过多个代理的情况，取第一个非 unknown 的有效IP字符串
            String[] ips = ip.split(COMMA);
            for (String s : ips) {
                if (!UNKNOWN.equalsIgnoreCase(s)) {
                    ip = s;
                    break;
                }
            }
        }
        return ip;
    }

    /**
     * 获取完整的请求路径, 包含 queryString
     */
    public static String getFullRequestUrl(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        if (StringUtils.isBlank(request.getQueryString())) {
            return request.getRequestURL().toString();
        }
        return request.getRequestURL().append('?').append(request.getQueryString()).toString();
    }

    /**
     * 获取完整的请求路径, 包含 queryString
     */
    public static String getFullRequestUrl(String requestUrl, String queryString) {
        if (StringUtils.isBlank(queryString) && StringUtils.isBlank(requestUrl)) {
            return "";
        }
        if (StringUtils.isBlank(queryString)) {
            return requestUrl;
        }
        return requestUrl + "?" + queryString;
    }


    /**
     * 从 request 中获取当前用户凭证
     */
    public static String getUserPrincipal(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        Principal principal = request.getUserPrincipal();
        if (principal != null && StringUtils.isNotBlank(principal.getName())) {
            return principal.getName();
        }
        return "";
    }
}
