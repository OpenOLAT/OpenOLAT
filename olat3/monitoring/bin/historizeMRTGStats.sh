#!/bin/bash
#####
# Skript to archive MRTG statistics for various servers on a daily or whatever base
# Install a cronjob that call this script:
#
## Example cronjob to archive daily (aprox 300kb uncompressed data = 100MB/year)
#
# 59 23 * * * historizeMRTGStats.sh
#
# 8. Feb 2007 gnaegi@frentix.com
#####

#ARCHIVEPATH=${mrtgbackup.dir}
ARCHIVEPATH=/usr/local/olatfs/olat/olatdata/logs/mrtgbackup

# GNU Wget 1.9.1
#wget --sslcheckcert=0 --http-user="${mrtg.loginname}" --http-passwd="${mrtg.pwd}" -nH -np -nd -nv -P $ARCHIVEPATH/`date '+%Y-%m-%d'` --mirror https://${server.domainname}/monitoring/mrtg/index.html

# GNU Wget 1.10.2
wget --no-check-certificate -e robots=off --user="${mrtg.loginname}" --password="${mrtg.pwd}" -nH -np -nd -nv -P $ARCHIVEPATH/`date '+%Y-%m-%d'` --mirror https://${server.domainname}/monitoring/mrtg/index.html
