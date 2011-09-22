#!/bin/sh

# zip rotated logs
cd /usr/local/opt/olat/olatdata/logs/
gzip -9 apache_access_log.txt.1
gzip -9 apache_error_log.txt.1
gzip -9 mod_jk.log.txt.1
gzip -9 monitoring_log.txt.1

# move rotated logs into archive folder
mv apache_access_log.txt.1.gz archive/apache_access_log.txt.$(date +"%Y%m%d").gz
mv apache_error_log.txt.1.gz archive/apache_error_log.txt.$(date +"%Y%m%d").gz
mv mod_jk.log.txt.1.gz archive/mod_jk.log.txt.$(date +"%Y%m%d").gz
mv monitoring_log.txt.1.gz archive/monitoring_log.txt.$(date +"%Y%m%d").gz
