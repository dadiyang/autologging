#!/bin/bash
cd autologging-core
mvn clean install -DskipTests
cd ../autologging-aop
mvn clean install -DskipTests
