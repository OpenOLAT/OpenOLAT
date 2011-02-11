#!/bin/bash
#
# This file outputs the number of apaches and mysql clients currently running 
# It can be used by mrtg to create nice images...
#
ps -ef | grep httpd | wc -l | sed 's/ *//g'
netstat -lapt | grep 'mysql' | grep 'java' | wc -l 
date
echo "${server.domainname}"
