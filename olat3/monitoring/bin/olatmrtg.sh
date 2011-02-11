#!/bin/bash

/usr/bin/mrtg --lock-file /tmp/monitoring_l --logging ${log.dir}/monitoring.log --confcache-file ${mrtgtmp.dir}/monitoring.ok ${monitoring.dir}/conf/mrtg.cfg

exit 0
