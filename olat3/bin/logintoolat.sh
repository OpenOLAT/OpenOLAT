#!/bin/bash

###
# Bash script based on basic linux command line tools to log in to OLAT start a course
# from the home and logout again.
# Install: Create a user with a password in OLAT and disable the AJAX mode for this user.
# This user should have one bookmark to a course on his home screen.
#
# initial date: 4. Sept. 2007
# author: Guido Schnider

## output in logfile should look like this, if not all url's are present olat is in ajax mode or the performace
# user does need the necessary *single* bookmark to a course which is mandatory! CHECK THIS!

# testing: idolats0.uzh.ch:8082 with user: performance-test-user-2
# START: 1233593485 (CUT_TIME: 11)
# Mon Feb 2 17:51:26 CET 2009 LOGIN_FORM /olat/dmz/1%3A2%3A2002298189%3A2%3A0%3Acid%3Alogin/?lp=OLAT
# Mon Feb 2 17:51:27 CET 2009 LOGIN /olat/dmz/1%3A3%3A2002298347%3A1%3A0/
# Mon Feb 2 17:51:31 CET 2009 LAUNCH_COURSE /olat/auth/1%3A2%3A2002298546%3A1%3A0%3Ar%3A0%3Ap%3Acmd.launch/
# Mon Feb 2 17:51:34 CET 2009 LOGOUT /olat/auth/1%3A3%3A2002298388%3A2%3A0%3Acid%3Alogout/
# STOP: 1233593495
# PROCESSTIME (STOP-START-SLEEPTIME(9s)): 1
# ===========================================

sendErrorSMS()
{
        for name in $receivers
        do
                echo `date`" error - olat server ($domain) not available by auto loginscript!" | /usr/bin/mail -s "OLAT performance alert!" ~R "id_olat@olat.uzh.ch" $name@sms.switch.ch
        done
}

sendPerformanceSMS()
{
        for name in $receivers
        do
                echo `date`"OLAT ($domain) is slow! Login->start course->logout took ($PROCESSTIME)s" | /usr/bin/mail -s "OLAT performance alert!" ~R "id_olat@olat.uzh.ch" $name@sms.switch.ch
        done
}

#Chooses random one of the entries below and tries to login
let "n=$RANDOM % 4"

#add http:// or https:// and port as well if needed
domains=("http://idolatc00.internal.uzh.ch:8084" "http://idolatc00.internal.uzh.ch:8085" "http://idolatc02.internal.uzh.ch:8082" "http://idolatc02.internal.uzh.ch:8083" "https://www.olat.uzh.ch")
domain=${domains[$n]}

let "n=$RANDOM % 2"
usernames=("test-user" "test-user-2")
username=${usernames[$n]}
password="secret"
cutTime="11"

## add SMS receivers

##HANDY01=12344556

HANDY02=12344556

HANDY03=12344556


#################################################
# ADD receivers here separated by blank!
#################################################
receivers="$HANDY01 $HANDY02 $HANDY03"

LOG="/var/log/OLATautologin.log"

echo "testing: $domain with user: $username" >> ${LOG}

start=`date +%s`
echo "START: ${start} (CUT_TIME: ${cutTime})" >> ${LOG} 
#grab loginpage and extract form action uri
wget -o temp-log0 -O temp-startpage.html --no-check-certificate --save-cookies cookies.txt --keep-session-cookies $domain/olat/dmz/
sleep 1

# THIS step is optional - ONLY needed to get the normal (username/pw) loginform to front when shibboleth login is active
#open normal login form page not shibboleth
loginuri=`grep '?lp=OLAT' temp-startpage.html | sed 's/^.*href="//' | sed 's/".*$//'`
echo `date`" LOGIN_FORM ${domain}${loginuri}" >> ${LOG}
#echo $loginuri 
wget -o temp-log1 -O temp-logform.html --no-check-certificate --save-cookies cookies.txt --keep-session-cookies --load-cookies cookies.txt $domain$loginuri
sleep 1

#must be 1 otherwise the server is not available
checkResponse=`grep 'name=\"loginForm\"' temp-logform.html | wc -l`
if [ $checkResponse == 1 ]
then
	uri=`grep 'name=\"loginForm\"' temp-logform.html | sed 's/^.*action="//' | sed 's/".*$//'`
	#echo $uri
	
	#login with extracted uri and post method
        echo `date`" LOGIN ${uri}" >> ${LOG}
	wget -o temp-log2 -O temp-home.html --no-check-certificate --save-cookies cookies.txt --keep-session-cookies --load-cookies cookies.txt --post-data="lf_login=$username&lf_pass=$password&olat_fosm_0=Login" $domain$uri
	#sleep to let the instant messenger connect
	sleep 3
	
	#start demo course from the homepage with direkt link, this only works if there a no other "cmd.launch" stuff is on the screen
	# best is to remove all other portlets except the bookmarks portlet
	courseuri=`grep 'cmd.launch\/' temp-home.html | sed 's/^.*href="//' | sed 's/".*$//'`
        echo `date`" LAUNCH_COURSE ${courseuri}" >> ${LOG}
	#echo $courseuri
	#start course
	wget -o temp-log3 -O temp-course.html --no-check-certificate --save-cookies cookies.txt --keep-session-cookies --load-cookies cookies.txt $domain$courseuri
	#sleep to let the instant messenger create a chatroom
	sleep 3
	#extract logout uri for later usage
	logouturi=`grep 'logout\/' temp-course.html | sed 's/^.*href="//' | sed 's/".*$//'`
	#echo $logouturi
	
	#logout
        echo `date`" LOGOUT ${logouturi}" >> ${LOG}
	wget -o temp-log4 -O temp-logout.html --no-check-certificate --save-cookies cookies.txt --keep-session-cookies --load-cookies cookies.txt $domain$logouturi
	sleep 1
	#send sms if time is too long
	rm temp-*
	rm cookies.txt
	stop=`date +%s`
        echo "STOP: ${stop}" >> ${LOG}
	#substract the sleep times
	echo `date`" time: "$(($stop-$start-9))
	PROCESSTIME=$(($stop-$start-9))
        echo "PROCESSTIME (STOP-START-SLEEPTIME(9s)): ${PROCESSTIME}" >> ${LOG}
	if [ $PROCESSTIME -gt $cutTime ]
	then
		echo "OLAT IS SLOW !!!" >> ${LOG}
		# only send sms after 6 o'clock in the morning (after restart phase and indexing)
		if [ `date +%H` -gt 5 ]
		then
			sendPerformanceSMS
		fi
	fi
else
        echo "OLAT NOT AVAILABLE BY AUTO LOGINSCRIPT" >> ${LOG}
	if [ `date +%H` -gt 5 ]
	then
		sendErrorSMS
	fi
fi

echo "===========================================" >> ${LOG}

