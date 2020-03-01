本模块用于通过 SpringAOP 框架**进行方法运行监控**，包括对 Controller、 Service 和 Mapper 层的监控日志，以及统一异常处理。

# 使用方式

## 依赖

### 在 SpringBoot 项目中

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
<dependency>
    <groupId>com.github.dadiyang</groupId>
    <artifactId>autologging-aop</artifactId>
    <version>1.0.0</version>
</dependency>
<!-- 开启了 kafka 上报日志，则必须添加此依赖  -->
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 普通 Spring 项目中

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-aop</artifactId>
    <version>${spring.version}</version>
</dependency>
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjrt</artifactId>
    <version>1.9.1</version>
</dependency>
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>1.9.1</version>
</dependency>
<dependency>
    <groupId>com.github.dadiyang</groupId>
    <artifactId>autologging-aop</artifactId>
    <version>1.0.0</version>
</dependency>
<!-- 开启了 kafka 上报日志，则必须添加此依赖  -->
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <version>1.0.0</version>
</dependency>
```
并在加了 `@Configuration` 的类中添加 `@EnableAspectJAutoProxy` 开启 AspectJ 功能支持

## 开启功能切面

### 开箱即用的通用切面

在加了 `@Configuration` 的类上根据自己的需要打上相应的注解即可。

| 注解 | 释义 | 备注|
|------|------|-----|
|@EnableMarkLog| 启用所有带 @MarkLog 注解的类和方法|
|@AutoLogAll| 开启全功能的自动日志|包含AutoLog、ControllerLog、KafkaLog、MapperLog、HttpApiInvokerLog 和 ServiceLog，需要提供 kafka 相关的配置|
|@AutoLogAllLocal| 开启全功能的自动日志，但只打印到本地不上报|跟 AutoLogAll 一样，但不包含 KafkaLog|
|@EnableKafkaLog| 开启上报日志到 kafka| 必须提供 `autolog.kafka.bootstrap-server`、`autolog.kafka.client-id` 和 `autolog.kafka.topic` 配置，并且可以使用 `autolog.kafka.enable` 做为开关|
|@EnableControllerLog| 开启 Controller 日志|
|@EnableMapperLog| 开启 Mapper 的日志| 带有 @Mapper 的类|
|@RepositoryLogAspect| 开启 Repository 的日志| 带有  @Repository 的类|
|@EnableServiceLog| 开启 Service 的日志| 带有 @Service 的类
|@EnableHttpApiInvokerLog|开启 http-api-invoker 代理的接口日志|带有 @HttpApi 的类|
|@IgnoreLog| 忽略某个类或方法|

# 注意事项：

* 这些注解开启都是全局的，若某些类或方法不想被切入，则可以打上 @IgnoreLog 进行排除
* 每个切面都可以通过配置 autolog.aspect-enable.切面名=false 来关闭指定切面，如: 
    * autolog.aspect-enable.controller=false
    * autolog.aspect-enable.mapper=false
    * autolog.aspect-enable.repository=false
    * autolog.aspect-enable.service=false
    * autolog.aspect-enable.service-contract=false
    * autolog.aspect-enable.markLog=false
    * autolog.aspect-enable.http-api=false
* 为方便统一日志查询，**必须**在配置项中添加 `autolog.appName=集群名`
* 若只想启用上报日志，不打印日志到本地，可以添加配置 `autolog.local.enable=false`
* remoteIp 字段用于查看调用方ip地址，需要在 nginx 中添加如下配置:

    ```text
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    ```
* 日志耗时阈值
    
    有时候我们可能只关心耗时较长的方法，这时可以通过 `autolog.time-consume-threshold` 或 `autolog.local.time-consume-threshold` 配置耗时阈值，前者是全局配置，后者仅对本地打印生效。
    
    注：方法抛异常时，不判断耗时

* 参数和返回值摘要

默认参数会全量打印，返回值则进行摘要

可以通过 `autolog.serialize.args-full` 和 `autolog.serialize.result-full` 来设置开关，通过 `autolog.serialize.args-max-length` 和 `autolog.serialize.result-max-length` 来配置摘要长度

# 配置项

以下为完整配置项及其默认值

```xml
# 请填写集群名
autolog.app-name=
# 是否读取远程配置，开启此配置将可以支持远程动态更改配置
autolog.use-remote=false
# 如果启用远程配置，必须配置远程服务器地址
autolog.remote-config-host=
# 耗时阈值，只有当方法耗时大于这个值时才处理，单位毫秒，默认不限制
autolog.time-consume-threshold=-1
# 是否打印日志到本地
autolog.local.enable=true
# 本地打印耗时阈值，只有当方法耗时大于这个值时打印到本地，单位毫秒，默认不限制
autolog.local.time-consume-threshold=-1
# 关于参数和返回值序列化的配置
autolog.serialize.args-full=true
autolog.serialize.args-max-length=512
autolog.serialize.result-full=false
autolog.serialize.result-max-length=512
# 选择开启哪些切面
autolog.aspect-enable.controller=true
autolog.aspect-enable.mapper=true
autolog.aspect-enable.repository=true
autolog.aspect-enable.service=true
autolog.aspect-enable.service-contract=true
autolog.aspect-enable.markLog=true
autolog.aspect-enable.http-api=true
# 若开启 kafka 上报，则以下四个选项为必填项
autolog.kafka.enable=false
autolog.kafka.bootstrap-server=
autolog.kafka.client-id=
autolog.kafka.topic=
# 以下 kafka 细节配置，如无特殊情况，请保持默认
autolog.kafka.batch-size=16384
autolog.kafka.acks=all
autolog.kafka.buffer-memory=33554432
autolog.kafka.metadata-max-age-ms=300000
autolog.kafka.max-block-ms=0
autolog.kafka.request-timeout-ms=30000
autolog.kafka.key-serializer=org.apache.kafka.common.serialization.StringSerializer
autolog.kafka.value-serializer=org.apache.kafka.common.serialization.StringSerializer
```

```yml
autolog:
    # 请填写集群名
    app-name: 
    # 是否读取远程配置，开启此配置将可以支持远程动态更改配置
    use-remote: false
    # 如果启用远程配置，必须配置远程服务器地址
    remote-config-host: ''
    # 耗时阈值，只有当方法耗时大于这个值时才处理，单位毫秒，默认不限制
    time-consume-threshold: -1
    # 是否打印日志到本地
    local: 
        enable: true
        # 本地打印耗时阈值，只有当方法耗时大于这个值时打印到本地，单位毫秒，默认不限制
        time-consume-threshold: -1
    # 选择开启哪些切面
    aspect-enable:
        controller: true
        http-api: true
        log: true
        mapper: true
        repository: true
        service: true
        service-contract: true
    # 关于参数和返回值序列化的配置
    serialize:
        args-full: true
        args-max-length: 512
        result-full: false
        result-max-length: 512
    kafka:
        # 若开启 kafka 上报，则以下四个选项为必填项
        enable: false
        bootstrap-server: 
        client-id: 
        topic: 
        # 以下 kafka 细节配置，如无特殊情况，请保持默认
        acks: all
        batch-size: 16384
        buffer-memory: 33554432
        max-block-ms: 0
        metadata-max-age-ms: 300000
        request-timeout-ms: 30000
        key-serializer: org.apache.kafka.common.serialization.StringSerializer
        value-serializer: org.apache.kafka.common.serialization.StringSerializer
```

若开启日志上报，需要 kafka 相关配置，请查看 **日志上报所需服务申请** 一节

# 方法调用链追踪

切面方法在执行的时候会生成一个唯一的 id，在同一个调用链路中的方法将拥有同一个 id，因此可以通过这个 id 来查看方法的调用链。

例如：

```text
dadiyangdeMacBook-Pro:logs dadiyang$ grep 38973078503424 server.log
04-01 18:46:36.211 ERROR [http-nio-8081-exec-4] $Proxy180:46 - 38973078503424 - HttpApi: 发生异常, com.github.dadiyang.biz.httpinvoker.OppReadApi.getReleasePubOpps(["55",5]), 耗时: 3
04-01 18:46:36.213 ERROR [http-nio-8081-exec-4] OpportunityReadServiceImpl:46 - 38973078503424 - Service: 发生异常, com.github.dadiyang.biz.service.opp.impl.OpportunityReadServiceImpl.getReleasePubOpps(["55",5]), 耗时: 6
04-01 18:46:36.214 ERROR [http-nio-8081-exec-4] OppReadController:62 - 38973078503424 - 请求发生异常,  http://localhost:8081/oppRead/releasePubOpps/55 com.github.dadiyang.biz.controller.OppReadController.getReleasePubOpps(["55",5]), 耗时: 7
```

从这个链路可以看出，OppReadController.getReleasePubOpps -调用-> OpportunityReadServiceImpl.getReleasePubOpps -调用-> OppReadApi.getReleasePubOpps

# 扩展

## 扩展切点

若需要切入除本项目提供的切面之外的其他切面，可以继承 `AbstractCommonLogAspect` 类，并实现 `pointcut()` 方法声明自己的切点

可参考 `ServiceLogAspect`、`MapperLogAspect` 和 `RepositoryLogAspect` 的实现

## 日志监听

可以通过实现 `LogTraceListener` 接口并注册为 Bean，这样在有新的日志被打印时就会被回调并得到一个 LogTrace 实例。

可参考 `KafkaLogTraceListener` 的实现

## 异常处理

可以通过实现 `ControllerExceptionHandler` 接口并注册为 Bean，可以接管 Controller 方法抛出的异常