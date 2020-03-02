#!/bin/bash
## 用于编译安装模块到默认的 sandbox 用户模块路径下
cd ../../autologging-core
mvn clean install -DskipTests
cd ../autologging-aop
mvn clean install -DskipTests
cd ../autologging-aop-jvm-sandbox
mvn clean package -DskipTests  
cp target/autologging-aop-jvm-sandbox-1.0.0-jar-with-dependencies.jar ~/.opt/sandbox/module/
