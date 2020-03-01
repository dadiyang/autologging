# autologging-aop-jvm-sandbox

基于 [jvm-sandbox](https://github.com/alibaba/jvm-sandbox) 框架实现的无侵入的可在运行时动态织入的 AOP 监控日志实现，功能与 [aop](../autologging-aop) 模块一样

这个模块非常神奇，它提供了一个能力，让应用完全无感的情况下，在运行时动态将监控日志相关代码织入到目标应用中，使之拥有输入监控日志的能力