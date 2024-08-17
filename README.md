# su-tools

执行打包命令
```
cd /root/code/su-tools/ \
&& git pull -p \
&& JAVA_HOME=/root/soft/jdk-22.0.2 mvn -f su-netty/pom.xml clean package -Dmaven.test.skip=true \
&& echo '当前目录' && pwd \
&& java22 -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5008 \
su-netty/target/su-netty-0.0.1-SNAPSHOT.jar

```