#!/bin/bash
#
# Run this script to check the image references in your layout.css file
# - checks if referenced images do really exist
# - checks if images exists that are not used in your CSS file (list all spare files)
# 
# NOTE:  run this script after SASS-compiling with style:extended (non-minified)
# 
# 18.05.2008 :: original script
# 02.01.2012 :: adjustments for new SASS themes introduced with OpenOLAT 8, strentini
#
# Florian Gnägi
# frentix GmbH
# http://www.frentix.com

# get CSSFILERAW base dir
THEMEDIR=`pwd`

# loop over all themes
for THEMERAW in $(find . -type d -maxdepth 1 -mindepth 1 \! -name "CVS"); do
	# remove ./ from THEMERAW, store as theme
	THEME=${THEMERAW:2}
	
	# do not check .sass-cache
	THEME_SUB=${THEME:0:11}
	
	if [ $THEME_SUB == ".sass-cache" ]
		then
			echo "skipping .sass-cache"
			continue
	fi
	
	cd $THEMEDIR/$THEME
	
	# we'll check only the compiled stylesheet file
	CSSFILE=layout.css
	
	echo "*************************************************************"	
	echo "*************************************************************"	
	echo "Processing theme \"$THEME\" in `pwd`"
	
	
	# first check for missing images
	echo ""
	echo "----------------------------------------------------------------"		
	echo "1) The following images are used in $CSSFILE but do not exist!" 
	echo "----------------------------------------------------------------"		
	
	# get all lines in file that have an image url() tag in it. 
	# note: url(), url("") and url('') ar supported;   _BUT_ only one url tag per css line supported, therefore use this script with a "non-minified" compiled css-file
	for CSSLINE in $(grep "url(" $CSSFILE ); do
			# remove stuff (before and after the url() tag, the url() itself), now the line contains a relative uri 
			RELPATH=$(echo $CSSLINE | awk 'BEGIN {FS="url\\("}{ print $2 }' | awk 'BEGIN {FS="\\)"}{ print $1 }')
			# remove leading and trailing doulblequotes, if any
			RELPATH=${RELPATH#\"}
			RELPATH=${RELPATH%\"}
			
			RELPATH=${RELPATH#\'}
			RELPATH=${RELPATH%\'}
			
			# check if this relpath does exist using an ls command
			if [ -n "$RELPATH" ]; then
				LS=`ls $RELPATH 2> /dev/null` 
				if [ "$LS" !=  "$RELPATH" ]; then
					# path does not exist
					echo "$RELPATH does not exist "
				fi	
			fi
		done
	echo ""
	
	exit 

	# now check for existing images, which are never referenced in layout.css
	
	echo "----------------------------------------------------------------"		
	echo "2) The following images exist but are not used in $CSSFILE"
	echo "----------------------------------------------------------------"	
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
	
done