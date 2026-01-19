#!/usr/bin/env bash
# Sample setenv.sh for Tomcat (Mac/Linux)
# Place this file content in TOMCAT_HOME/bin/setenv.sh and make it executable

export CATALINA_OPTS="-XX:+UseG1GC -Xms512m -Xmx2048m -Djava.awt.headless=true -Dfile.encoding=UTF-8"

# Additional options you may want while developing
# export CATALINA_OPTS="$CATALINA_OPTS -Dolat.debug=true -Dlog.rootCategory=DEBUG,console"
