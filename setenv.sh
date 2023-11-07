CATALINA_HOME=/usr/local/tomcat
CATALINA_BASE=/home/openolat
CATALINA_PID=/usr/local/tomcat/run/openolat.pid
CATALINA_TMPDIR=/tmp/openolat
mkdir -p $CATALINA_TMPDIR
CATALINA_OPTS=" \
-Xmx1024m -Xms512m -XX:MaxMetaspaceSize=512m \
-Duser.name=openolat \
-Duser.timezone=Europe/Zurich \
-Dspring.profiles.active=myprofile \
-Djava.awt.headless=true \
-Dolat.log.dir=/usr/local/tomcat/logs \
-Djava.net.preferIPv4Stack=true \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=. \
"