#!/bin/bash
dfv=`df -P | grep /disks/olatng/livefs | awk '{print $3}'`
duv=-1
if [ -r ${mrtgtmp.dir}/du_olatlivefs.txt ] ; then
  duv=`cat ${mrtgtmp.dir}/du_olatlivefs.txt | awk '{print $1}'`
fi
[ -z "${dfv}" ] && dfv=0
[ -z "${duv}" ] && duv=0
echo "${dfv}"
echo "${duv}"
date
echo "${server.domainname}"