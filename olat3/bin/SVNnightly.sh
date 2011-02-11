#!/bin/sh

echo "NIGHTLY START"
# "** Sending DE changes diffs to ale"
/usr/local/opt/nightly/bin/notify_de_diffs.sh

#echo "** Processing translation DE"
/usr/local/opt/nightly/bin/SVNcheckin_translations.sh de
#echo "** Processing translation EN"
/usr/local/opt/nightly/bin/SVNcheckin_translations.sh en
#echo "** Processing translation FR"
/usr/local/opt/nightly/bin/SVNcheckin_translations.sh fr
#echo "** Processing translation IT"
/usr/local/opt/nightly/bin/SVNcheckin_translations.sh it
#echo "** Processing translation ES"
/usr/local/opt/nightly/bin/SVNcheckin_translations.sh es
#echo "** Processing translation GR"
/usr/local/opt/nightly/bin/SVNcheckin_translations.sh gr
#echo "** Processing translation CZ"
/usr/local/opt/nightly/bin/SVNcheckin_translations.sh cz
#echo "** Processing translation PL"
/usr/local/opt/nightly/bin/SVNcheckin_translations.sh pl
#echo "** Processing translation RU"
/usr/local/opt/nightly/bin/SVNcheckin_translations.sh ru
#echo "** Processing translation DK"
/usr/local/opt/nightly/bin/SVNcheckin_translations.sh dk

export JAVA_HOME=/usr/local/opt/java-1.5
export ANT_HOME=/usr/local/opt/ant
export CATALINA_HOME=/usr/local/opt/nightly/tomcat
export LANG=de_CH


echo "** Stopping Tomcat..."
$CATALINA_HOME/bin/shutdown.sh

cd /usr/local/opt/nightly/olat4/
#cd /usr/local/opt/nightly/olatlive/

echo "** Running nightly build..."
ant -logfile ../build.log -logger org.apache.tools.ant.listener.MailLogger SVNnightly

echo "** run ant config-deploy-all"
ant config-deploy-all

echo "** Starting Tomcat..."
cd /usr/local/opt/nightly/olatdata/logs
$CATALINA_HOME/bin/startup.sh
echo "NIGHTLY STOP"