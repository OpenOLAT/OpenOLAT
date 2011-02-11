#!/bin/bash
#
# olat_backup.sh - olat backup script
#
# script name           : olat_backup.sh
# script version        : 1.2.4
# script author         : OLAT/ID
# script created        : 2004
# script changed        : 2008/08/12
# 
# notes
# -----
# 
# Backup script. RUN AS ROOT
#
# change history
# --------------
#
# 08/08/13 version 1.2.4
#  - message handling now instance-dependent
# 08/08/12 version 1.2.3
#  - bugfixed logrotate logfile nameing
# 08/08/07 version 1.2.2
#  - added activemq handling
# 08/08/05 version 1.2.1
#  - updated instances
# 08/07/08 version 1.2.1
#  - instance base dir now /usr/local/olatlocal for all
#  - added new instances :
#    olat01,olat02,olat03,olat04,olatsh01
# 08/03/11 version 1.2.0
#  - changed commands
#  - bugfixed commandline handling
#  - adapted changes from old olatbackup script version 1.1.7
#  - runs sharedmemstat --allusers --clean if present before
#    service restart
#  - restartMysql splitted into start/stop and wait functions
#  - print out netstat status of port 8009 to logfile while waiting
# 08/01/26 version 1.1.6-ng
# - changed for olat-ng layout
# 07/11/23 version 1.1.5-ng
# - changed for olat-ng layout
# 07/11/22 version 1.1.5
# - added --optimize option taht causes execution
#   of 'mysqlcheck -Aao1' before dumping the databases
# 07/11/14 version 1.1.4
# - save gc.log before stopping tomcat. If saving after, it's empty. (asch)
# 07/11/09 version 1.1.3
#  - check for existance of gc.log before copying it
#  - corrected syslogrc definition
# 07/10/05 version 1.1.2
#  - before stoping tomcat save garbage collector log (asch)
# 07/09/25 version 1.1.1
#  - restartmysql now calls logrotate and forces a
#    syslog restart to make sure
# 07/08/23 version 1.1.0
#  - major rewrite
#  - auto-detect mysql databases and password
# 07/08/23 version 1.0.6
#  - jivemessenger rename to messaging
# 07/06/22 version 1.0.5
#  - do not call mysqldump with xcall
# 07/06/21 version 1.0.4
#  - xcall now logs to file
#  - argument parsing for --test etc
# 07/06/20 version 1.0.3
#  - setup for idmels04
#  - wildfire renamed to openfire
# 06/11/09 version 1.0.2
#  - setup for idmels02, handle all instances
# 06/08/24 version 1.0.1
#  - wait 120 secs before restarting mysql
# 06/08/22 version 1.0.0
#  - added forced apache kill
#  - added version information
#

# 
# script info
#

sname="olat_backup.sh"
sversion="1.2.4"
sdate="2008/08/13"
sinfo="olat backup script"
scpr="copyright (c) 2004..2008 by OLAT/ID"
scall="$0"

# 
# script setup
#

busleep=120
bumessagewait=600
buinstances="id olat01 olat02 olat03 olat04 olatsh01"
budir=/usr/local/var/backups
bulogfile="/var/log/${sname}.message.log"

# used to construct instance message script  file path
instanceBaseDir="/usr/local/olatlocal"
instanceMessage="olatlive/bin/olat_backup_message.sh"

defaultPasswd=""
defaultList="mysql test olat messaging"
apachebin="/usr/sbin/httpd2-prefork"
mysqlrc="/etc/init.d/mysql"

forceLogrotate=0
logrotatebin="/usr/sbin/logrotate"
logrotateopt="" # set to "-v" for more info
logrotateconf="/etc/logrotate.conf"
logrotatelog="/var/log/${sname}.logrotate.log"

forceSyslogRestart=0
syslogrc="/etc/init.d/syslog"
sharedmemstatbin="/usr/local/sbin/sharedmemstat"

verbose=1
logging=1
testMode=0
allowDefaults=0
doHelp=0
doVersion=0
doDump=0
doStart=0
doStop=0
doMessage=0
doOptimize=0
warnMsg=""
errMsg=""
errCode=0

#
# basic functions
#

xecho()
{
  [ ${verbose} -gt 0 ] && echo -e $@
  [ ${logging} -gt 0 ] && echo -e $@ >> ${bulogfile}
}

xcall()
{ 
  if [ ${testMode} -ne 0 ] ; then 
    xecho "  TEST: $@"
  elif [ ${logging} -gt 0 ] ; then
    eval $@  2>&1 | tee -a ${bulogfile}
  else
    eval $@
  fi
}   

#
# database functions
#

getDatabaseList()
{
  dbUser="root"
  dbList=""
  xecho -n " - reading password ... "
  dbPasswd=`cat /etc/my.cnf | awk '$1=="password" && $2=="=" { print $3 }'`
  ##cat /etc/my.cnf | awk '$1=="password" && $2=="=" { print $3 }'
  if [ -z "${dbPasswd}" ] && [ ${allowDefaults} -gt 0 ] ; then
    ${dbPasswd}=${defaultPasswd}
    warnMsg="${errMsg}WARNING: could not detect mysql password, using default\n"
  fi
  if [ -z "${dbPasswd}" ] ; then
    errMsg="${errMsg}ERROR: could not detect mysql password\n"
    xecho "[error]"
  else
    xecho "[done]"
  fi
  
  xecho -n " - reading database list ... "
  dbList=`mysql --user ${dbUser} --password="${dbPasswd}" -s -e "show databases ;" 2>/dev/null | grep -v Database | tr [:space:] ' '`
  if [ -z "${dbList}" ] && [ ${allowDefaults} -gt 0 ] ; then
    dbList="${defaultList}"
    warnMsg="${errMsg}WARNING: could not detect mysql password, using default\n"
  fi
  if [ -z "${dbList}" ] ; then
    errMsg="${errMsg}ERROR: could not get database list from mysql\n"
    xecho "[error]"
  else
    xecho "[done]"
  fi
  
  if [ -n "${warnMsg}" ] ; then
    echo -e ${warnMsg};
    warnMsg=""
  fi
  if [ -n "${errMsg}" ] ; then
    echo -e "${errMsg}"
    exit 1
  fi
  xecho "  - databases to backup: ${dbList}"
}

runOptimize()
{
  [ ${doOptimize} -le 0 ] && return
  xecho "`date '+%D-%T'`: optimizing mysql databases"
  stat=0
  dt=`date '+%s'`
  if [ ${testMode} -gt 0 ] ; then
    xecho "TEST: mysqlcheck --user ${dbUser} --password=${dbPasswd} -Aao1"
  else
    mysqlcheck --user ${dbUser} --password=${dbPasswd} -Aao1
    stat=$?
  fi
  dt=$(( `date '+%s'` - ${dt} ))
  xecho "`date '+%D-%T'`: optimization finished after ${dt} second(s), status=${stst}"
}
  
#
# tomcat handling
#

setupMessage()
{
  [ ${doStop} -le 0 ] && return 
  [ ${doMessage} -le 0 ] && return 
  xecho "`date '+%D-%T'`: setting up service messages for tomcat instances"
  doWait=0
  for instance in ${buinstances} ; do
    if [ "${instance}" = "id" ] ; then 
      iname="olat"
    else
      iname=${instance}
    fi
    idir=${instanceBaseDir}/${iname}
    bum=${idir}/${instanceMessage}

    if [ -x "${bum}" ] ; then
      xecho -n "`date '+%D-%T'`: setup service message for instance ${instance} ... "
      # countdown for shutting down tomcat
      xcall nohup "${bum}" >> ${bulogfile} 2>&1 &
      xecho "[done]" 
      doWait=${bumessagewait}
    else 
      xecho "WARNING: could not find \"${bum}\", skipping"
    fi
  done

  if [ ${doWait} -gt 0 ] ; then
    ## messages are set, now wait
    xecho -n "`date '+%D-%T'`: waiting for message countdown (${doWait} sec(s)) ... "
    xcall sleep ${doWait}
    xecho "[done]" 
  fi
}

startTomcat()
{
  [ ${doStart} -le 0 ] && return 
  for instance in ${buinstances} ; do
    # starting tomcats
    xecho "`date '+%D-%T'`: starting up tomcat instance ${instance}"
    if [ -x "/etc/init.d/tomcat_${instance}" ] ; then
      xcall /etc/init.d/tomcat_${instance} start --debug
    else 
      xecho "WARNING: could not find \"/etc/init.d/tomcat_${instance}\", skipping"
    fi
  done
}

stopTomcat()
{
  [ ${doStop} -le 0 ] && return 

  for instance in ${buinstances} ; do
    # shutting down tomcats
    xecho "`date '+%D-%T'`: stopping tomcat instance ${instance}"
    if [ -x "/etc/init.d/tomcat_${instance}" ] ; then
      xcall /etc/init.d/tomcat_${instance} stop --debug
      xcall /etc/init.d/tomcat_${instance} kill --debug
      xcall /etc/init.d/tomcat_${instance} KillJava --debug
    else 
      xecho "WARNING: could not find \"/etc/init.d/tomcat_${instance}\", skipping"
    fi
  done
}

#
# openfire handling
#

startOpenfire()
{
  [ ${doStart} -le 0 ] && return 
  xecho "`date '+%D-%T'`: starting openfire"
  xcall /etc/init.d/openfire_id start --debug
  xcall sleep 30
}

stopOpenfire()
{
  [ ${doStop} -le 0 ] && return 
  xecho "`date '+%D-%T'`: shut down openfire"
  if [ -x "/etc/init.d/openfire_id" ] ; then
    xcall /etc/init.d/openfire_id stop --debug
    xcall /etc/init.d/openfire_id kill --debug
    xcall /etc/init.d/openfire_id KillJava --debug
  else 
    xecho "WARNING: could not find \"/etc/init.d/openfire_id\", skipping"
  fi
}

#
# activemq handling
#

startActiveMQ()
{
  [ ${doStart} -le 0 ] && return 
  xecho "`date '+%D-%T'`: starting activemq"
  xcall /etc/init.d/activemq_id start --debug
  xcall sleep 30
}

stopActiveMQ()
{
  [ ${doStop} -le 0 ] && return 
  xecho "`date '+%D-%T'`: shut down activemq"
  if [ -x "/etc/init.d/activemq_id" ] ; then
    xcall /etc/init.d/activemq_id stop --debug
    xcall /etc/init.d/activemq_id kill --debug
    xcall /etc/init.d/activemq_id KillJava --debug
  else 
    xecho "WARNING: could not find \"/etc/init.d/activemq_id\", skipping"
  fi
}

#
# java handling
#

javaKillAll()
{
  [ ${doStop} -le 0 ] && return 
  xecho "`date '+%D-%T'`: one to kill em all "
  if [ -x "/etc/init.d/tomcat_id" ] ; then
    xcall /etc/init.d/tomcat_id KillAllJava --debug --force
  else 
    xecho "WARNING: could not find \"/etc/init.d/tomcat_id\", skipping"
  fi
}

#
# apache handling
#

startApache()
{
  [ ${doStart} -le 0 ] && return 
  xecho "`date '+%D-%T'`: starting apache"
  xcall /etc/init.d/apache2 start
}

stopApache()
{
  [ ${doStop} -le 0 ] && return 
  ## forced apache murder
  xecho "`date '+%D-%T'`: shutdown apache"
  xcall /etc/init.d/apache2 stop
  if [ -f /var/run/httpd2.pid ] ; then 
    echo "WARNING: found /var/run/httpd2.pid"
    kpid=`cat /var/run/httpd2.pid` 
    if [ -n "${kpid}" ] ; then
      echo "WARNING: apache not stopped - using kill -KILL ${kpid}" 
      xcall kill -KILL ${kpid}
      xcall sleep 1
    fi
  fi
  kpid=`ps -e -o pid,comm | grep httpd | grep -v grep | awk '{ print $1 }' | tr [:space:] ' '`
  if [ -n "${kpid}" ] ; then
    echo "WARNING: apache not stopped - using kill -KILL ${kpid}" 
    xcall kill -KILL ${kpid}
    xcall sleep 1
  fi
  kpid=`ps -e -o pid,comm | grep httpd | grep -v grep | awk '{ print $1 }' | tr [:space:] ' '`
  if [ -n "${kpid}" ] ; then
    echo "WARNING: apache still not stopped - using killall -KILL ${apachebin}" 
    xcall killall -KILL ${apachebin}
    xcall sleep 1
  fi
  kpid=`ps -e -o pid,comm | grep httpd | grep -v grep | awk '{ print $1 }' | tr [:space:] ' '`
  if [ -n "${kpid}" ] ; then
    echo "ERROR: apache still not killed - giving up" 
  else
    echo "NOTICE: apache is dead now - cleanup" 
    xcall rm -f found /var/run/httpd2.pid
  fi
}

#
# mysql handling
#

stopMysql()
{
  [ ${doStop} -le 0 ] && return 
  xecho "`date '+%D-%T'`: stopping mysql"
  xcall ${mysqlrc} stop
}

startMysql()
{
  [ ${doStart} -le 0 ] && return 
  xecho "`date '+%D-%T'`: start mysql"
  xcall ${mysqlrc} start
}

dumpMysql()
{
  [ ${doDump} -le 0 ] && return
  if [ -n "${budir}" ] ; then
    xcall mkdir -p ${budir}/last
    ##
    for instance in ${dbList} ; do
      if [ -f ${budir}/${instance}.sql.gz ] ; then
        xecho "`date '+%D-%T'`: saving last dump to ${budir}/last"
        xcall rm -f ${budir}/last/${instance}.sql.gz
        xcall mv ${budir}/${instance}.sql.gz ${budir}/last
      fi
      xecho "`date '+%D-%T'`: dumping mysql database ${instance}"
      if [ ${testMode} -gt 0 ] ; then
        xecho "TEST: mysqldump --user ${dbUser} --password=${dbPasswd} ${instance} | gzip > ${budir}/${instance}.sql.gz"
      else
        mysqldump --user ${dbUser} --password="${dbPasswd}" ${instance} | gzip > ${budir}/${instance}.sql.gz
      fi
    done
  fi
}

#
# other tools
#

forceLogrotate()
{
  [ ${doStop} -le 0 ] && return 

  if [ ${forceLogrotate} -gt 0 ] ; then 
    xecho "`date '+%D-%T'`: force a logrotate"
    cmd="${logrotatebin} ${logrotateopt} ${logrotateconf}"
    if [ ${testMode} -ne 0 ] ; then 
      xecho "  TEST: $cmd"
    else
      rm -f ${logrotatelog}
      touch ${logrotatelog}
      if [ -w "${logrotatelog}" ] ; then
        xecho "`date '+%D-%T'`: ${cmd}"
        eval ${cmd} > ${logrotatelog} 2>&1
      else
        xecho "`date '+%D-%T'`: WARNING - could not write to ${logrotatelog}"
        xcall ${cmd}
      fi
    fi
    xecho "`date '+%D-%T'`: --------------------------"
  fi
  
  if [ ${forceSyslogRestart} -gt 0 ] ; then 
    xecho "`date '+%D-%T'`: force a syslog restart"
    xcall ${syslogrc} restart
    xecho "`date '+%D-%T'`: --------------------------"
  fi  
}

cleanSharedMemory()
{
  [ ${doStop} -le 0 ] && return 
  if [ ! -x "${sharedmemstatbin}" ] ; then
    xecho "`date '+%D-%T'`: ${sharedmemstatbin} not found, skipping shared memory cleanup"
    return
  fi
  xecho "`date '+%D-%T'`: shared memory cleanup:"
  xcall ${sharedmemstatbin} --allusers --clean
}

pauseUntilRestart()
{
  [ ${doStart} -gt 1 ] && [ ${doStop} -gt 1 ] && return  
  [ -z "${busleep}" ] && return
  xecho "`date '+%D-%T'`: waiting for recovery sleep (${busleep} sec(s))"
  secs=0
  while [ ${secs} -lt ${busleep} ] ; do
   if [ $(( ${secs} % 10 )) -eq 0 ] ; then
     date '+%D-%T' >> ${bulogfile}
     netstat -antp | grep 8009 >> ${bulogfile}
   fi
   [ ${testMode} -eq 0 ] && sleep 1
   secs=$(( ${secs} + 1 ))
  done
  xecho "`date '+%D-%T'`: done"
}

### 
###  main program starts here
### 

for arg in $@ ; do
  case "${arg}" in
    --allowdefaults)   allowDefaults=1      ;;
    --backup)          doStart=1 ; doStop=1 ; doOptimize=1 ; doDump=1; doMessage=1  ;;
    --dump)            doDump=1             ;;
    --dumponly)        doStart=0 ; doStop=0 ; doOptimize=0 ; doDump=1; doMessage=0  ;;
    --message)         doMessage=1          ;;
    --help)            doHelp=1             ;;
    --logrotate)       forceLogrotate=1     ;;
    --nologrotate)     forceLogrotate=0     ;;
    --nomessage)       doMessage=0          ;;
    --nooptiomize)     doOptimize=0         ;;
    --nosleep)         busleep=10           ;;
    --nosyslogrestart) forceSyslogRestart=1 ;;
    --notest)          testMode=0           ;;
    --nowait)          bumessagewait=10     ;;
    --optimize)        doOptimize=1         ;;
    --optimizeonly)    doStart=0 ; doStop=0 ; doOptimize=1 ; doDump=0; doMessage=0  ;;
    --restartonly)     doStart=1 ; doStop=1 ; doOptimize=0 ; doDump=0; doMessage=0  ;;
    --startonly)       doStart=1 ; doStop=0 ; doOptimize=0 ; doDump=0; doMessage=0  ;;
    --stoponly)        doStart=0 ; doStop=1 ; doOptimize=0 ; doDump=0; doMessage=0  ;;
    --syslogrestart)   forceSyslogRestart=1 ;;
    --test)            testMode=1           ;;
    --version)         doVersion=1          ;;
    *)             [ -n "${arg}" ] && errMsg="${errMsg}ERROR: invalid argument \"${arg}\"\n" ;;
  esac
done

action=$(( ${doStart} + ${doStop} + ${doDump} + ${doOptimize} + ${doHelp} + ${doVersion} ))

na=$(( ${action} + ${doHelp} + ${doVersion} ))

if [ ${na} -le 0 ] ; then
  errMsg="${errMsg}ERROR: missing argument(s)\n"
fi

touch "${bulogfile}"
[ -f "${bulogfile}" ] || logging=0

xecho "\n### ${sname} version ${sversion} - ${sdate}\n### ${sinfo}\n" 
[ ${doVersion} -gt 0 ] && exit 0

if [ -n "${errMsg}" ] ; then
  echo -e "${errMsg}"
  doHelp=1
  errCode=2
fi

if [ ${doHelp} -gt 0 ] ; then
  echo -e "Usage: ${sname} [--options]"
  echo " avaliable options are : "
  echo " --allowdefaults)    allow run with default setting (handle with care)" 
  echo " --backup)           full backup (stop/optimize/dump/start)" 
  echo " --dump              enable dump" 
  echo " --dumponly          dump only" 
  echo " --help              show this help" 
  echo " --message           enable service message "
  echo " --logrotate         enable forced logrotate"
  echo " --nologrotate       disable forced logrotate"
  echo " --nomessage         disable service message "
  echo " --nooptiomize       disable optimization"
  echo " --nosleep           reduce sleep after backup (120->10s)"
  echo " --nosyslogrestart   disable forced syslog restart"
  echo " --notest            disable test mode"
  echo " --nowait            reduce message hold time (600->10s)"
  echo " --optimize          enable optimization"
  echo " --optimizeonly      optimize only" 
  echo " --restartonly       restart services only"  
  echo " --startonly         start services only"  
  echo " --stoponly          stop services only"  
  echo " --syslogrestart     enable forced syslog restart"
  echo " --test              run in test mode"
  echo " --version           show version and exit"
  echo 
  exit ${errCode}
fi

getDatabaseList

setupMessage

## save garbage collector log
DATE=`date +%Y-%m-%d`
[ -f /scratch/tmp/gc.log ] && cp /scratch/tmp/gc.log /scratch/tmp/gc.log_${DATE}

stopTomcat
stopOpenfire
stopActiveMQ
javaKillAll

stopApache

runOptimize
dumpMysql

stopMysql

forceLogrotate
cleanSharedMemory
pauseUntilRestart

startMysql

startApache
startActiveMQ
startOpenfire
startTomcat

xecho "`date '+%D-%T'`: finished\n"
exit ${errCode}

