# AutoLogging 监控日志框架

本框架支持通过 AOP 对程序进行方法级别的监控和调用链路追踪，将监控信息打印到本地或通过 Kafka 消息上报

# 项目背景

在开发过程中，我们常常会看到这样的代码: 

```java
@GetMapping("{id}")
public ReturnDTO<User> getById(int id) {
    log.info("根据主键id获取权限, id: {}", id);
    long startTime = System.currentTimeMillis();
    try {
        ReturnDTO<User> rs =  ResultUtil.successResult(userService.getById(id));
        log.debug("根据主键id获取权限成功, id: {}, 耗时: {}", id, (System.currentTimeMills() - startTime));
        return rs;
    } catch (Exception e) {
        log.error("据主键id获取权限发生异常, id: {}, 耗时: {}", id, (System.currentTimeMills() - startTime), e);
        return ResultUtil.errorResult("据主键id获取权限发生异常, id: " + id);
    }
}
```
这种写法有以下几个问题：
* 代码冗余，不易维护
* 日志格式不统一，难以自动化分析
* 容易遗漏或者写错日志信息

这些问题都给**开发、测试和线上排查问题造成困扰**

想象一下，整个项目当中，我们有多少个像这样的方法？DRY！

# 项目特点

只需要添加 maven 依赖，然后给项目打上一个注解，就能开启所有功能，我们就可以去掉所有监控日志，由框架统一输出：

```java
@GetMapping("{id}")
public ReturnDTO<User> getById(int id) {
    return ResultUtil.successResult(userService.getById(id));
}
```

自动日志框架会自动输出方法的监控日志

通过 kafka 消息，我们可以将日志上报到 ElasticSearch 中，直接在 Kibana 中进行统一查看

# 效果展示

```text
2020-03-01 13:38:46.483  INFO 54635 --- [           main] c.g.d.a.c.l.LocalLogTraceListener        : 121361440260956160 | 2 | Repository |  | com.github.dadiyang.autologging.test.user.UserMapperFakeImpl | getById | [830293] | {"id":830293,"username":"张三"} | 4
2020-03-01 13:38:46.484  INFO 54635 --- [           main] c.g.d.a.c.l.LocalLogTraceListener        : 121361440260956160 | 1 | Service |  | com.github.dadiyang.autologging.test.user.UserServiceImpl | getById | [830293] | {"id":830293,"username":"张三"} | 27
2020-03-01 13:38:46.485  INFO 54635 --- [           main] c.g.d.a.c.l.LocalLogTraceListener        : 121361440260956160 | 0 | Controller | GET 127.0.0.1 http://localhost/user/getById   | com.github.dadiyang.autologging.test.user.UserController | getById | [830293] | {"id":830293,"username":"张三"} | 39
2020-03-01 13:38:46.528 ERROR 54635 --- [           main] c.g.d.a.c.l.LocalLogTraceListener        : 121361440592306176 | 2 | Repository | | com.github.dadiyang.autologging.test.user.UserMapperFakeImpl | updateById | [{"id":830293,"username":"张三"}] | 0 | java.lang.UnsupportedOperationException: 模拟抛出异常
	at com.github.dadiyang.autologging.test.user.UserMapperFakeImpl.updateById(UserMapperFakeImpl.java:14)
	at com.github.dadiyang.autologging.test.user.UserMapperFakeImpl$$FastClassBySpringCGLIB$$8b95b1d3.invoke(<generated>)
	at org.springframework.cglib.proxy.MethodProxy.invoke(MethodProxy.java:218)
```

# 项目结构

本框架分为 core、aop、aop-jvm-sandbox、test 四个项目，其中 

* [core](./autologging-core) 核心模块，包含配置和上报相关的日志监听器的定义;
* [aop](./autologging-aop) 通过 SpringAOP **打印和上报方法执行的监控信息**，包括对 Controller、 Service 和 Mapper 层的监控日志，以及统一异常处理;
* [aop-jvm-sandbox](./autologging-aop-jvm-sandbox) 基于 [jvm-sandbox](https://github.com/alibaba/jvm-sandbox) 框架实现的无侵入的 AOP 日志实现，它提供了一个能力，让应用完全**无感的情况下，在运行时动态将监控日志相关代码织入到目标应用中，使之拥有输入监控日志的能力**
* [test](./autologging-test) 用于测试

# 快速开始

## 基于 aop 的方法级监控日志

以 SpringBoot 环境使用为例

### maven 依赖

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
```

### 注解

在 Application 类上添加 @EnableServiceLog 注解开启 Service 层的监控

各个切面有预置的注解，可以根据需要挑选添加：

| 注解 | 释义 | 备注|
|------|------|-----|
|@EnableMarkLog| 启用所有带 @MarkLog 注解的类和方法|在需要监控的类或方法上添加 @MarkLog 
|@AutoLogAll| 开启所有切面的日志监控功能，打印到本地，如果有引入 Kafka 相关配置则进行上报||
|@AutoLogAllLocal| 开启所有切面的日志监控功能，打印到本地||
|@EnableControllerLog| 开启 Controller 日志|
|@EnableServiceLog| 开启 Service 的日志| 带有 @Service 的类
|@EnableMapperLog| 开启 Mapper 层的日志| 带有 @Mapper 的类|
|@RepositoryLogAspect| 开启 Repository 的日志| 带有  @Repository 的类|

### 配置

```yaml
autolog:
    # 请填写应用名称，必填！
    app-name: 
```

OK，搞定！启动之后应用中的各个切面方法执行的时候就会打印相关的日志了。

更多配置项请查看：[autologging-aop](./autologging-aop)