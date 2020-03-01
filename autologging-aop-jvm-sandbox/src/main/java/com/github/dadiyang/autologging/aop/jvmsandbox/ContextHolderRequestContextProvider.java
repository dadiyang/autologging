package com.github.dadiyang.autologging.aop.jvmsandbox;

import com.github.dadiyang.autologging.aop.handler.RequestContextProvider;
import com.github.dadiyang.autologging.aop.jvmsandbox.util.RequestContextHolder;
import com.github.dadiyang.autologging.aop.util.RequestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 基于 ContextHolder 获取请求上下文
 *
 * @author dadiyang
 * @since 2019/3/1
 */
public class ContextHolderRequestContextProvider implements RequestContextProvider {
    @Override
    public HttpServletRequest getRequest() {
        return RequestContextHolder.getContext() == null ? null : RequestContextHolder.getContext().getRequest();
    }

    @Override
    public HttpServletResponse getResponse() {
        return RequestContextHolder.getContext() == null ? null : RequestContextHolder.getContext().getResponse();
    }

    @Override
    public String getUsername() {
        return RequestUtils.getUserPrincipal(getRequest());
    }
}
