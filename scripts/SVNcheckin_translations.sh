#!/bin/bash
# Checks for changes in the translation directory, updates them and checks
# them in to the OLAT svn
# 19.11.2004
# gnaegi@id.unizh.ch
# updated to use with svn, 11.4.2006, alexander.schneider@id.unizh.ch


if [ "$1" = "" ]; then
 echo "Usage: checkin_translations.sh LOCALE Example: checkin_translations.sh en"
 exit;
fi

#read locale as input parameter
LOCALE=$1

#LOG=/usr/local/olat/translationlog/$LOCALE-trans-$(date +%Y%m%d).log
LOG=/usr/local/opt/nightly/translationlog/$LOCALE-trans-$(date +%Y%m%d).log

cd /usr/local/opt/nightly/olat4/
#cd /usr/local/opt/nightly/olatlive/

echo -e "Starting SVN update for locale $LOCALE\n" >> $LOG
echo -e "---------------------------------------\n" >> $LOG
UPDATE=`svn update webapp/i18n/default/$LOCALE/` > /dev/null 2> /dev/null
echo $UPDATE | grep "^M.*" >> $LOG
echo $UPDATE | grep "^P.*" >> $LOG

CONFLICTS=`echo $UPDATE | grep "^C.*"` 2> /dev/null
# abort when any conflicts are found and send email to developers
if [ "$CONFLICTS" != "" ]; then
  echo "****** Conflicts found in" >> $LOG
  echo $CONFLICTS >> $LOG
  echo "****** Fix them manually !!" >> $LOG
  echo $CONFLICTS | mail -s "Conflicts found while updating locale $LOCALE - fix them" -r "olat@olat.unizh.ch" id_olat@id.unizh.ch
  exit;
fi


echo -e "Starting SVN diffs for locale $LOCALE\n" >> $LOG
echo -e "-------------------------------------\n" >> $LOG
svn diff webapp/i18n/default/$LOCALE/ >> $LOG 2> /dev/null

# TODO: check for conflicts here, proceed only when no conflicts are here

echo -e "Checking in changes for locale $LOCALE\n" >> $LOG
echo -e "------------------ -------------------\n" >> $LOG
svn ci -m "Translation changes in $LOCALE" webapp/i18n/default/$LOCALE/ >> $LOG 2> /dev/null

echo -e "\n\n" >> $LOG
