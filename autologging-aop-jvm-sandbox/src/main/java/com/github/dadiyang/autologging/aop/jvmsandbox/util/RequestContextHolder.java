package com.github.dadiyang.autologging.aop.jvmsandbox.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 通过 ThreadLocal 保存当前的 HttpServletRequest 和 HttpServletResponse 对象
 *
 * @author dadiyang
 * @since 2019/3/1
 */
public class RequestContextHolder {
    private static final ThreadLocal<Context> REQUEST_THREAD_LOCAL = new ThreadLocal<>();

    public static Context getContext() {
        return REQUEST_THREAD_LOCAL.get();
    }

    public static void remove() {
        REQUEST_THREAD_LOCAL.remove();
    }

    public static void set(Context context) {
        REQUEST_THREAD_LOCAL.set(context);
    }

    /**
     * 请求上下文，包含 request 和 response
     */
    public static class Context {
        private final HttpServletRequest request;
        private final HttpServletResponse response;

        public Context(HttpServletRequest request, HttpServletResponse response) {
            this.request = request;
            this.response = response;
        }

        public HttpServletRequest getRequest() {
            return request;
        }

        public HttpServletResponse getResponse() {
            return response;
        }
    }
}