#!/bin/bash

# script that checks for unused properties in all spring config files.
# start script outside olat3 / olatcore folder but names must match olat3 olatcore
#
# date: 17.01.2011
# autor: Guido Schnider

declare -A proptofile

for prop in `cat ./olat3/webapp/WEB-INF/src/serviceconfig/olat.properties | grep -v '^$' | grep -v '#' | cut -d= -f1`
do
  #echo $prop
  proptofile["$prop"]=0
done

for file in `find olat3 olatcore -name '*.xml'`
do
	for prop in `cat ./olat3/webapp/WEB-INF/src/serviceconfig/olat.properties | grep -v '^$' | grep -v '#' | cut -d= -f1`
	do
		found=`grep $prop $file | wc -l`
		if [ $found -gt 0 ]; then
			times=${proptofile[$prop]}
			total=$(( $times + $found ))
			proptofile["$prop"]=$total
			#echo "$prop found $found time(s) in $file"
		fi

	done
done

echo ""
echo " Properties with a "- 0" are not used in xml spring files. Ignore .values and .comment prop as they are only used for comments"
echo ""

for property in ${!proptofile[@]}
do
	echo "$property - ${proptofile[$property]}"
done