#!/bin/bash
# Checks for changes in the translation directory, updates them and checks 
# them in to the OLAT cvs
# 19.11.2004
# gnaegi@id.unizh.ch

export CVSROOT=:ext:nightly@cvs.olat.org:/usr/local/cvs/
export CVS_RSH=ssh

DEDIFFSNOTIFICATION="webmaster@myolat.com"

cd /usr/local/opt/nightly/olatlive/webapp/WEB-INF

find src -type f -name 'LocalStrings_de*' -print0 | xargs -0 cvs diff >dediff.diff 2>/dev/null

DIFF=`cat dediff.diff`

# abort when any conflicts are found and send email to developers
if [ "$DIFF" != "" ]; then
   cat dediff.diff | mail -s "Changes found in DE locale - notify translators if needed" -r olat@olat.unizh.ch $DEDIFFSNOTIFICATION
fi
rm dediff.diff
