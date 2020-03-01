package com.github.dadiyang.autologging.aop.handler;

import com.github.dadiyang.autologging.aop.aspect.JoinPointInfo;
import com.github.dadiyang.autologging.aop.util.RequestUtils;
import com.github.dadiyang.autologging.core.condition.ConditionalOnClass;
import com.github.dadiyang.autologging.core.configuration.AutoLogConfig;
import com.github.dadiyang.autologging.core.logtrace.LogTrace;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 继承自 CommonLogJoinPointHandler，主要是添加 Controller 特有的信息记录等操作
 * <p>
 * 负责处理 Controller 切点方法的执行之后记录请求的详细信息及统一异常处理逻辑
 *
 * @author dadiyang
 * @since 2019/3/1
 */
@Slf4j
@Setter
@Component
@ConditionalOnClass(name = {"javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse"})
public class ControllerLogJoinPointHandler extends CommonLogJoinPointHandler {
    private RequestContextProvider requestContextProvider;

    public ControllerLogJoinPointHandler(AutoLogConfig autoLogConfig, RequestContextProvider requestContextProvider) {
        super(autoLogConfig);
        this.requestContextProvider = requestContextProvider;
    }

    @Autowired
    public ControllerLogJoinPointHandler(ApplicationContext applicationContext,
                                         RequestContextProvider requestContextProvider) {
        super(applicationContext);
        this.requestContextProvider = requestContextProvider;

    }

    @Override
    protected LogTrace getLogTrace(JoinPointInfo joinPointInfo) {
        LogTrace logTrace = joinPointInfo.toLogTrace(autoLogConfig.getAppName());
        // 如果开启全参数或者抛出异常，则获取参数全文
        AutoLogConfig.SerializeConfig serializeConfig = autoLogConfig.getSerialize();
        // 如果配置使用全参数或者抛出了异常则使用全参数
        boolean af = serializeConfig == null || serializeConfig.getArgsFull() || joinPointInfo.getThrowable() != null;
        logTrace.setArgs(serializerWrapper.argsToString(joinPointInfo.getArgs(), af));
        // 配置明确指定为 true，才使用全返回值
        boolean rf = serializeConfig != null && serializeConfig.getResultFull();
        logTrace.setResult(serializerWrapper.resultToString(joinPointInfo.getResult(), rf));
        HttpServletRequest request = requestContextProvider.getRequest();
        // 获取得到 request 对象才添加请求相关字段，否则退化为普通的切面
        if (request != null) {
            logTrace.setUrl(RequestUtils.getFullRequestUrl(request));
            logTrace.setUserPrincipal(requestContextProvider.getUsername());
            logTrace.setRequestMethod(request.getMethod());
            logTrace.setRemoteIp(RequestUtils.getIpAddress(request));
        }
        return logTrace;
    }
}