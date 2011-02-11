#!/bin/bash

#
# mrtg statistics: get current logins every 5 minutes
#

wget --no-check-certificate --bind-address=127.0.0.1 https://${server.domainname}/olat/stats.html?cmd=users            -O ${mrtgtmp.dir}/olat_users_stats            >>/var/log/crontab.log 2>>/var/log/crontab.log
wget --no-check-certificate --bind-address=127.0.0.1 https://${server.domainname}/olat/stats.html?cmd=webdav           -O ${mrtgtmp.dir}/olat_webdav_stats           >>/var/log/crontab.log 2>>/var/log/crontab.log
wget --no-check-certificate --bind-address=127.0.0.1 https://${server.domainname}/olat/stats.html?cmd=imstats          -O ${mrtgtmp.dir}/olat_imstats_stats          >>/var/log/crontab.log 2>>/var/log/crontab.log
wget --no-check-certificate --bind-address=127.0.0.1 https://${server.domainname}/olat/stats.html?cmd=debug            -O ${mrtgtmp.dir}/olat_debug_stats            >>/var/log/crontab.log 2>>/var/log/crontab.log
wget --no-check-certificate --bind-address=127.0.0.1 https://${server.domainname}/olat/stats.html?cmd=mem              -O ${mrtgtmp.dir}/olat_vmmem_stats            >>/var/log/crontab.log 2>>/var/log/crontab.log
wget --no-check-certificate --bind-address=127.0.0.1 https://${server.domainname}/olat/stats.html?cmd=proc             -O ${mrtgtmp.dir}/olat_proc_stats             >>/var/log/crontab.log 2>>/var/log/crontab.log
wget --no-check-certificate --bind-address=127.0.0.1 https://${server.domainname}/olat/stats.html?cmd=err              -O ${mrtgtmp.dir}/olat_err_stats              >>/var/log/crontab.log 2>>/var/log/crontab.log
wget --no-check-certificate --bind-address=127.0.0.1 https://${server.domainname}/olat/stats.html?cmd=usercount        -O ${mrtgtmp.dir}/olat_usercount_stats        >>/var/log/crontab.log 2>>/var/log/crontab.log
wget --no-check-certificate --bind-address=127.0.0.1 https://${server.domainname}/olat/stats.html?cmd=usercountmonthly -O ${mrtgtmp.dir}/olat_usercountmonthly_stats >>/var/log/crontab.log 2>>/var/log/crontab.log
wget --no-check-certificate --bind-address=127.0.0.1 https://${server.domainname}/olat/stats.html?cmd=usercountdaily   -O ${mrtgtmp.dir}/olat_usercountdaily_stats   >>/var/log/crontab.log 2>>/var/log/crontab.log
wget --no-check-certificate --bind-address=127.0.0.1 https://${server.domainname}/olat/stats.html?cmd=coursecount      -O ${mrtgtmp.dir}/olat_coursecount_stats      >>/var/log/crontab.log 2>>/var/log/crontab.log

#
# fetch ping statistics every 5 minutes
#
${monitoring.dir}/bin/mrtg-ping-probe.pl -k 10 www.google.com >>/var/log/crontab.log 2>>/var/log/crontab.log

exit 0
