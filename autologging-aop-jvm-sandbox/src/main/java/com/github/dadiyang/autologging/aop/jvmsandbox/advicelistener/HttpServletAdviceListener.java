package com.github.dadiyang.autologging.aop.jvmsandbox.advicelistener;

import com.alibaba.jvm.sandbox.api.listener.ext.Advice;
import com.alibaba.jvm.sandbox.api.listener.ext.AdviceListener;
import com.github.dadiyang.autologging.aop.jvmsandbox.util.InterfaceProxyUtils;
import com.github.dadiyang.autologging.aop.jvmsandbox.util.RequestContextHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * 拦截 javax.servlet.http.HttpServlet.service 方法获取当前 request 和 response 对象，放到 context 中
 *
 * @author dadiyang
 * @since 2019/3/1
 */
public class HttpServletAdviceListener extends AdviceListener {
    private static final Logger log = LoggerFactory.getLogger(HttpServletAdviceListener.class);
    private static final String DOT = ".";
    private static final List<String> STATIC_TYPES = Arrays.asList("css", "js", "json", "ico", "png", "img", "jpg",
            "xls", "xlsx", "pdf", "gif", "mp3", "mp4", "avi", "amr");

    @Override
    protected void before(Advice advice) {
        // 只关心顶层调用
        if (!advice.isProcessTop()) {
            return;
        }
        HttpServletRequest request = InterfaceProxyUtils.puppet(HttpServletRequest.class, advice.getParameterArray()[0]);
        String uri = request.getRequestURI();
        // 排除静态资源文件请求
        if (uri.contains(DOT)) {
            String[] parts = uri.split("\\" + DOT);
            if (STATIC_TYPES.contains(parts[parts.length - 1])) {
                return;
            }
        }
        String traceId = request.getHeader("auto_logging_trace_id");
        if (StringUtils.isNumeric(traceId)) {
            AutoLoggingAdviceListener.setTraceId(Long.parseLong(traceId));
        }
        HttpServletResponse response = InterfaceProxyUtils.puppet(HttpServletResponse.class, advice.getParameterArray()[1]);
        // 保存 request 对象
        log.debug("设置 request 上下文");
        RequestContextHolder.set(new RequestContextHolder.Context(request, response));
    }

    @Override
    protected void afterReturning(Advice advice) {
        // 只关心顶层调用
        if (!advice.isProcessTop()) {
            return;
        }
        clearRequest();
    }

    private void clearRequest() {
        log.debug("移除 request 上下文");
        RequestContextHolder.remove();
    }

    @Override
    protected void afterThrowing(Advice advice) {
        // 只关心顶层调用
        if (!advice.isProcessTop()) {
            return;
        }
        clearRequest();
    }
}
