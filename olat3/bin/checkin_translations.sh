#!/bin/bash
# Checks for changes in the translation directory, updates them and checks
# them in to the OLAT cvs
# 19.11.2004
# gnaegi@id.unizh.ch

export CVSROOT=:ext:nightly@cvs.olat.org:/usr/local/cvs/
export CVS_RSH=ssh

LOG=/usr/local/opt/nightly/translationlog/trans-$(date +%Y%m%d).log

cd /usr/local/opt/nightly/olatlive

echo -e "Starting CVS update for all locales \n" >> $LOG
echo -e "---------------------------------------\n" >> $LOG
UPDATE=`find . -type f -name 'LocalStrings_*' | xargs cvs -n update` > /dev/null 2> /dev/null
echo $UPDATE | grep "^M.*" >> $LOG
echo $UPDATE | grep "^P.*" >> $LOG

CONFLICTS=`echo $UPDATE | grep "^C.*"` 2> /dev/null
# abort when any conflicts are found and send email to developers
if [ "$CONFLICTS" != "" ]; then
  echo "****** Conflicts found in" >> $LOG
  echo $CONFLICTS >> $LOG
  echo "****** Fix them manually !!" >> $LOG
  echo $CONFLICTS | mail -s "Conflicts found while updating - fix them" -r "olat@olat.unizh.ch" id_olat@id.unizh.ch
  exit;
fi


echo -e "Starting CVS diffs for all locales \n" >> $LOG
echo -e "-------------------------------------\n" >> $LOG
cvs diff webapp/WEB-INF/src/ >> $LOG 2> /dev/null

# TODO: check for conflicts here, proceed only when no conflicts are here

echo -e "Checking in changes for all locales \n" >> $LOG
echo -e "------------------ -------------------\n" >> $LOG
find /usr/local/opt/nightly/olatlive/webapp/WEB-INF/src/org/olat/ -type f -name 'LocalStrings_*' -print0 | xargs -0 cvs add >> $LOG
find /usr/local/opt/nightly/olatlive/webapp/WEB-INF/src/org/olat/ -type f -name 'LocalStrings_*' | xargs cvs ci -m "Translation changes" >> $LOG 2> /dev/null >> $LOG
echo -e "\n\n" >> $LOG
