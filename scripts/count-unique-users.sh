#!/bin/sh
## counts unique logins out of one or more olat log files.
## Usage: count-unique-users.sh filename1 filename2 ...
for l in `ls $1` 
do
awk 'BEGIN{FS="\\^%\\^"}/Logged.on/{split($1,tmp," ");start=index($9,"[");end=index($9,"]");msg=substr($9,start+1,end-start-1);print msg}' $l | sort -u | wc -l
echo $res
done

