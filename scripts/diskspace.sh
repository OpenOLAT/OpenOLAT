#!/bin/sh
# Generates a disk usage report
# 19.11.2004
# gnaegi@id.unizh.ch

TOMAIL="olat@olat.unizh.ch"
FROMMAIL="id_olat@id.unizh.ch"

cd /usr/local/opt/olat/olatdata/
rm -f quota.txt

uname -a >> quota.txt
echo -e "-------------------------------------\n" >> quota.txt

echo -e "Disk Usage of olatdata directory" >> quota.txt
echo -e "-------------------------------------\n" >> quota.txt
du -hs --exclude=bcroot --exclude=quota.txt--exclude=quota.txt  ??* >> quota.txt
du -h --max-depth=1 bcroot >> quota.txt
du -hs . >>quota.txt

echo -e "\n\nFilesystem Totals" >> quota.txt
echo -e "-------------------------------------\n" >> quota.txt
df -h >> quota.txt

echo -e "\n\nTop-10 space waster statistics (in MB)" >> quota.txt
echo -e "-------------------------------------\n" >> quota.txt
echo -e "Top 10 course folder:" >> quota.txt
du --block-size=1MB --max-depth=1 bcroot/course/ | sort -gr | head -n 11 | tail -n 10 >> quota.txt
echo -e "\nTop 10 user homes:" >> quota.txt
du --block-size=1MB --max-depth=1 bcroot/homes/ | sort -gr | head -n 11 | tail -n 10 >> quota.txt
echo -e "\nTop 10 group folders:" >> quota.txt
du --block-size=1MB --max-depth=1 bcroot/cts/folders/BusinessGroup/ | sort -gr | head -n 11 | tail -n 10 >> quota.txt
echo -e "\nTop 10 repository entries:" >> quota.txt
du --block-size=1MB --max-depth=1 bcroot/repository/ | sort -gr | head -n 11 | tail -n 10 >> quota.txt

less quota.txt 2>&1 | mail -s "Nightly OLAT disk usage report" -r $FROMMAIL $TOMAIL 
