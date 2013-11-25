#!/bin/sh
#
# Compilescript for openolat-SASS-theme
# Can be included into eclipse-project-build (project-properties --> "Builders")
# 
# It compiles the whole directory and mostly  all changed files. 
# In the case it does not compile all files, it's helpful to delete the sass-cache (rm -r .sass-cache).
#
# usage:
# ./compiletheme.sh										# compile themes in the current directory
# ./compiletheme.sh /path/to/your/custom/theme			# compile themes in the given directory
# ./compiletheme.sh /path/to/your/custom/themedir		# compile all themes in the given directory
#
####

# Configuration
STYLE=compressed
#STYLE=compact

# Helper method to add theme to array
searchThemes () {
	if [ ! -z $1  ];
	then
	    if [ -f $1/layout.scss  ];
	    then
	    	doCompile $1
	    elif [ -d $1  ];
	    then
			for FILE in $1/*
			do
				searchThemes $FILE
			done
		fi
	fi
}

# Helper method to compile the theme
doCompile () {
	echo "Compiling SASS: $1 $STYLE"
	sass --version
	sass --style $STYLE --update $1:$1 --load-path openolat openolat/all openolat/all/modules openolat/print openolat/mobile
	echo "done"
}


# Add themes to compile from given path
doCompile ".";
searchThemes $1;
echo "DONE"
