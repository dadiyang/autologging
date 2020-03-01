package com.github.dadiyang.autologging.aop.handler;

import com.github.dadiyang.autologging.aop.util.RequestUtils;
import com.github.dadiyang.autologging.core.condition.ConditionalOnClass;
import lombok.RequiredArgsConstructor;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 通过给定的当前的 request 对象以获取相关信息
 * <p>
 * Spring会自动通过构造器注入当前 request 对象
 *
 * @author dadiyang
 * @since 2019/3/1
 */
@RequiredArgsConstructor
@ConditionalOnClass(name = {"javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse"})
public class DefaultRequestContextProvider implements RequestContextProvider {

    @Override
    public HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    @Override
    public HttpServletResponse getResponse() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
    }

    @Override
    public String getUsername() {
        return RequestUtils.getUserPrincipal(getRequest());
    }
}
