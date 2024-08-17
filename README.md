# su-tools

执行打包命令
```
 JAVA_HOME=/root/soft/jdk-22.0.2 mvn clean package

cd /root/code/su-tools/su-netty

java22 -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5008 target/su-netty-0.0.1-SNAPSHOT.jar
```