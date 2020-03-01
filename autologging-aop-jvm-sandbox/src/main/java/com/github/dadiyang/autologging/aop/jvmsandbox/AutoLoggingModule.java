package com.github.dadiyang.autologging.aop.jvmsandbox;

import com.alibaba.jvm.sandbox.api.Information;
import com.alibaba.jvm.sandbox.api.Module;
import com.alibaba.jvm.sandbox.api.ModuleLifecycle;
import com.alibaba.jvm.sandbox.api.annotation.Command;
import com.alibaba.jvm.sandbox.api.filter.AccessFlags;
import com.alibaba.jvm.sandbox.api.listener.ext.Advice;
import com.alibaba.jvm.sandbox.api.listener.ext.AdviceListener;
import com.alibaba.jvm.sandbox.api.listener.ext.EventWatchBuilder;
import com.alibaba.jvm.sandbox.api.resource.ModuleEventWatcher;
import com.github.dadiyang.autologging.aop.jvmsandbox.advicelistener.AutoLoggingAdviceListener;
import com.github.dadiyang.autologging.aop.jvmsandbox.advicelistener.AutoLoggingControllerAdviceListener;
import com.github.dadiyang.autologging.aop.jvmsandbox.advicelistener.HttpServletAdviceListener;
import com.github.dadiyang.autologging.aop.jvmsandbox.util.InterfaceProxyUtils;
import com.github.dadiyang.autologging.core.configuration.AutoLogConfig;
import com.github.dadiyang.autologging.core.configuration.KafkaProducerConfiguration;
import com.github.dadiyang.autologging.core.listener.KafkaLogTraceListener;
import com.github.dadiyang.autologging.core.listener.LocalLogTraceListener;
import com.github.dadiyang.autologging.core.listener.LogTraceListener;
import com.github.dadiyang.autologging.core.serializer.FastJsonSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 自动日志切面模块
 *
 * @author dadiyang
 * @since 2019/3/1
 */
@MetaInfServices(Module.class)
@Information(id = "auto-logging", author = "dadiyang", version = "0.0.1")
public class AutoLoggingModule implements Module, ModuleLifecycle {
    private static final Logger log = LoggerFactory.getLogger(AutoLoggingModule.class);
    private KafkaProducer<String, String> kafkaProducer;
    @Resource
    private ModuleEventWatcher moduleEventWatcher;

    @Command("load")
    public void load(Map<String, String> configMap) throws IOException {
        // 初始化全局配置项
        initConfig(configMap);
        List<LogTraceListener> logTraceListeners = addLogTraceListeners();
        log.info("模块加载完成，开启自动日志切面监控");
        log.info("注册 Controller 切面");
        // 监听 httpServlet 以获取当前 request 对象
        httpServletRequestAdvice();
        urlConnectionAdvice();
        // Controller 包含的切面
        String[] controllerAnnotation = new String[]{Constant.ANNOTATION_REST_CONTROLLER, Constant.ANNOTATION_CONTROLLER, Constant.ANNOTATION_RS_PATH};
        AutoLoggingControllerAdviceListener controllerAdviceListener
                = new AutoLoggingControllerAdviceListener(logTraceListeners, controllerAnnotation);
        cutByAnnotation(controllerAdviceListener);
        log.info("注册 Service 切面");
        cutByAnnotation(new AutoLoggingAdviceListener(Constant.ASPECT_SERVICE, logTraceListeners, Constant.ANNOTATION_SERVICE));
        log.info("注册 Mapper 切面");
        cutByAnnotation(true, new AutoLoggingAdviceListener(Constant.ASPECT_MAPPER, logTraceListeners, Constant.ANNOTATION_MAPPER));
        log.info("注册 Repository 切面");
        cutByAnnotation(new AutoLoggingAdviceListener(Constant.ASPECT_REPOSITORY, logTraceListeners, Constant.ANNOTATION_REPOSITORY));
        log.info("注册 HttpApi 切面");
        cutByAnnotation(true, new AutoLoggingAdviceListener(Constant.ASPECT_HTTP_API, logTraceListeners, Constant.ANNOTATION_HTTP_API));
        log.info("注册 MarkLog 切面");
        cutByAnnotation(true, new AutoLoggingAdviceListener(Constant.ASPECT_LOG, logTraceListeners, Constant.ANNOTATION_LOG));
        log.info("自动日志切面加载完成");
    }

    private void urlConnectionAdvice() {
        new EventWatchBuilder(moduleEventWatcher)
                .onClass("sun.net.www.protocol.http.HttpURLConnection")
                .includeBootstrap()
                .includeSubClasses()
                .onBehavior("connect")
                .onWatch(new AdviceListener() {
                    @Override
                    protected void before(Advice advice) throws Throwable {
                        Long id = AutoLoggingAdviceListener.getTraceId();
                        if (id != null) {
                            // 在建立 http 连接之前设置 traceId 到请求头中
                            URLConnection conn = InterfaceProxyUtils.puppet(URLConnection.class, advice.getTarget());
                            conn.addRequestProperty("auto_logging_trace_id", String.valueOf(AutoLoggingAdviceListener.getTraceId()));
                        }
                    }
                });
    }

    private interface URLConnection {
        void addRequestProperty(String key, String value);
    }

    private void initConfig(Map<String, String> configMap) throws IOException {
        String configPath = configMap.get("configPath");
        if (StringUtils.isNotBlank(configPath)) {
            if (configPath.startsWith("classpath:")) {
                try (InputStream in = getClass().getClassLoader().getResourceAsStream(configPath.replace("classpath:", ""))) {
                    Constant.init(in);
                }
            } else {
                try (InputStream in = new FileInputStream(configPath)) {
                    Constant.init(in);
                }
            }
        } else {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("application.properties")) {
                if (in != null) {
                    Constant.init(in);
                } else {
                    Constant.init(configMap);
                }
            }
        }
    }

    private List<LogTraceListener> addLogTraceListeners() {
        AutoLogConfig autoLogConfig = Constant.getAutoLogConfig();
        log.info("配置加载完成, {}", Constant.getAutoLogConfig());
        List<LogTraceListener> listeners = new LinkedList<>();

        // 没有关闭 kafka 上报日志且相关配置都已提供，则添加 kafka 日志监听器
        if (autoLogConfig.getKafka().getEnable()) {
            KafkaProducerConfiguration kafkaProducerConfiguration = new KafkaProducerConfiguration(autoLogConfig.getKafka());
            KafkaLogTraceListener kafkaLogTraceListener = kafkaProducerConfiguration.kafkaLogTraceListener(new FastJsonSerializer());
            kafkaProducer = kafkaLogTraceListener.getKafkaProducer();
            listeners.add(kafkaLogTraceListener);
        }

        // 添加本地日志监听器
        if (autoLogConfig.getLocalConfig().getEnable()) {
            listeners.add(new LocalLogTraceListener(autoLogConfig));
        }
        return listeners;
    }

    private void httpServletRequestAdvice() {
        // 通过拦截 javax.servlet.http.HttpServlet 的 service 方法，获取 HttpServletRequest 对象
        new EventWatchBuilder(moduleEventWatcher)
                .onClass("javax.servlet.http.HttpServlet")
                .includeSubClasses()
                .onBehavior("service")
                .withParameterTypes(
                        "javax.servlet.http.HttpServletRequest",
                        "javax.servlet.http.HttpServletResponse"
                )
                .onWatch(new HttpServletAdviceListener());
    }

    private void cutByAnnotation(AutoLoggingAdviceListener adviceListener) {
        cutByAnnotation(false, adviceListener);
    }

    /**
     * @param includeSub 动态代理类一般需要包含子类
     */
    private void cutByAnnotation(boolean includeSub, AutoLoggingAdviceListener adviceListener) {
        EventWatchBuilder builder = new EventWatchBuilder(moduleEventWatcher);
        EventWatchBuilder.IBuildingForBehavior buildingForBehavior = null;
        for (String type : adviceListener.getAnnotationNames()) {
            // 表示或关系
            EventWatchBuilder.IBuildingForClass buildingForClass = builder.onAnyClass()
                    .withAccess(AccessFlags.ACF_PUBLIC).hasAnnotationTypes(type);
            if (includeSub) {
                buildingForClass.includeSubClasses();
            }
            buildingForBehavior = buildingForClass.onAnyBehavior().withAccess(AccessFlags.ACF_PUBLIC);
        }
        if (buildingForBehavior != null) {
            buildingForBehavior
                    .onWatching()
                    .onWatch(adviceListener);
        }
    }

    @Override
    public void onUnload() throws Throwable {
        log.info("模块 auto-logging 卸载");
        if (kafkaProducer != null) {
            // 在模块卸载的时候关闭 kafka 连接
            try {
                log.info("关闭 kafka 连接");
                kafkaProducer.close(30, TimeUnit.SECONDS);
                log.info("kafka 连接已关闭");
            } catch (Exception e) {
                log.error("关闭 kafka 连接发生异常", e);
            }
        }
    }

    @Override
    public void onLoad() throws Throwable {
        log.debug("模块 auto-logging 加载");
        load(Collections.emptyMap());
    }

    @Override
    public void onActive() throws Throwable {
        log.debug("模块 auto-logging 激活");
    }

    @Override
    public void onFrozen() throws Throwable {
        log.debug("模块 auto-logging 冻结");
    }

    @Override
    public void loadCompleted() {
        log.debug("模块 auto-logging 加载完成");
    }
}
