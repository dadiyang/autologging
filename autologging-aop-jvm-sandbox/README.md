# autologging-aop-jvm-sandbox

基于 [jvm-sandbox](https://github.com/alibaba/jvm-sandbox) 框架实现的无侵入的可在运行时动态织入的 AOP 监控日志实现，功能与 [aop](../autologging-aop) 模块一样

这个模块非常神奇，它提供了一个能力，让应用完全无感的情况下，在运行时动态将监控日志相关代码织入到目标应用中，使之拥有输入监控日志的能力

# 使用方法

## 安装JVM-SANDBOX

```shell script
# 下载 JVM—SANDBOX
wget http://ompc.oss-cn-hangzhou.aliyuncs.com/jvm-sandbox/release/sandbox-stable-bin.zip
# 解压
unzip sandbox-stable-bin.zip
cd sandbox
# 安装到本地
./install-local.sh
# 下载 autologging-aop-jvm-sandbox 模块
wget https://github.com/dadiyang/autologging/releases/download/1.0.0/autologging-aop-jvm-sandbox-1.0.0-jar-with-dependencies.jar
# 安装到 JVM-SANDBOX 中
cp autologging-aop-jvm-sandbox-1.0.0-jar-with-dependencies.jar ~/.opt/sandbox/module/
```