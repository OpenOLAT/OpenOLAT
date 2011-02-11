#!/bin/bash

/usr/bin/du -s /disks/olatng/livefs > ${mrtgtmp.dir}/du_olatlivefs.txt 2>>/var/log/crontab.log

exit 0
