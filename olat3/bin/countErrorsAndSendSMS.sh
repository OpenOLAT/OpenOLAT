# send sms to users when the error count is bigger than specified
# only works with Switch.ch sms service!
# path to OLAT log files
cd /usr/local/opt/olat/olatdata/logs

# add numbers here
HANDY02=0041*********
HANDY03=0041*********
HANDY04=0041*********

CUTVALUE=180
ERRORCOUNT=`grep 'OLAT::ERROR' olat.log | wc -l`
if [ "$ERRORCOUNT" -gt "$CUTVALUE" ]
then
echo "Error count ($ERRORCOUNT) bigger than cut value ($CUTVALUE)." | /usr/bin/mail -s "OLAT alert!" -r "id_olat@olat.unizh.ch" $HANDY02@sms.switch.ch $HANDY03@sms.switch.ch $HANDY04@sms.switch.ch
fi
