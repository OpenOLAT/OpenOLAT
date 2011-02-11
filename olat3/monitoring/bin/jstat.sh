#!/bin/bash
# $1 (e.g. olat01) must be delivered by the parameter
if [ "$1" == "" ]; then
	echo ERROR: You must have set a parameter to get the tomcat process id
	exit	
fi
PID=`cat ${tomcat.home}/logs/tomcat_$1.pid`
DATA=`/usr/local/opt/java/bin/jstat -gcutil $PID | grep -v FGC`
FGC=-1
FGCT=-1
if [ -n "${DATA}" ] ; then
  FGC=`echo ${DATA} | awk '{print $8}'`
  FGCT=`echo ${DATA} | awk '{print $9}' | cut -d. -f1`
  [ -z "${FGC}" ] && FGC=0
  [ -z "${FGCT}" ] && FGCT=0
fi
echo $FGC
echo $FGCT
date
echo "${server.domainname}"