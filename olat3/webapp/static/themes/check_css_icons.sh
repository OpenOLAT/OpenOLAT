#!/bin/bash
#
# Run this script to check the image references in your CSS files
# - checks if images exists that are not used in your CSS files
# - checks if the referenced images do really exist
#
# 18.05.2008
# Florian Gnägi
# frentix GmbH
# http://www.frentix.com

# get CSSFILERAW base dir
THEMEDIR=`pwd`

# loop over all themes
for THEMERAW in $(find . -type d -maxdepth 1 -mindepth 1 \! -name "CVS"); do
	# remove ./ from THEMERAW, store as theme
	THEME=${THEMERAW:2}
	cd $THEMEDIR/$THEME
	echo "*************************************************************"	
	echo "*************************************************************"	
	echo "Processing theme \"$THEME\" in `pwd`"
	echo "-------------------------------------------------------------"		
	echo "The following images exist but are not used in the CSS files:"
	echo "-------------------------------------------------------------"	
	# get all graphics files and check if they are referenced in any css file	
	for IMGPATHRAW in $(find . -name "*.png" -or -name "*.gif" -or -name "*.jpg"); do
		# remove ./ from IMGPATHRAW, store as theme
		IMGPATH=${IMGPATHRAW:2}
		# check if a css file exist that contains the image path
		RESULT=`find . -name "*.css" | xargs grep -n $IMGPATH`
		if [ -z "$RESULT" ]; then
			# test was empty -> image not referenced
			echo $THEME/$IMGPATH
		fi
	done
	echo ""
	echo "============================================================="		
	echo "2) The following images are used in CSS files but do not exist" 
	echo "-------------------------------------------------------------"		
	# get all css files, starting in dir level 2 (on the theme level the css files contain only include statements)
	for CSSFILE in $(find . -name "*.css" -mindepth 2); do
		echo "** Checking $CSSFILE ..."
		# parse directory path from css file to be able to check relative image path correctness later
		DIR=$(echo $CSSFILE | awk 'BEGIN {FS="/"}{print $2}' | awk 'BEGIN {FS="/"}{print $1}')
		# get all lines in file that have an image url() tag in it. 
		# note: url() is supported, url("") is _not_ supported, only one url tag pre css line supported 
		for CSSLINE in $(grep "url(" $CSSFILE ); do
			# remove stuff (before and after the url() tag, the url() itself), now the line contains a relative uri 
			RELPATH=$(echo $CSSLINE | awk 'BEGIN {FS="url\\("}{ print $2 }' | awk 'BEGIN {FS="\\)"}{ print $1 }')
			# check if this relpath does exist using an ls command
			if [ -n "$RELPATH" ]; then
				LS=`ls $DIR/$RELPATH 2> /dev/null` 
				if [ "$LS" !=  "$DIR/$RELPATH" ]; then
					# path does not exist
					echo "$RELPATH defined in $CSSFILE but file does not exist"
				fi	
			fi
		done
	done
	echo ""
done