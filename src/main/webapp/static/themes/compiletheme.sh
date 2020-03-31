#!/bin/bash
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
export SASS_PATH=.

# Configuration
STYLE=compressed
#STYLE=compact
#default update command for sass
UPDATECMD="--update"

# Helper method to add theme to array
searchThemes () {
	if [ ! -z $1  ];
	then
	    if [ -f $1/theme.scss  ];
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
	TARGET=$1
	UPDATE=$UPDATECMD
	if [ $1 = "."  ];
	then
		TARGET="light"
	if [[ "--watch" == $UPDATECMD && ! -z $THEMES ]]; then UPDATE="--update"; fi
	fi
	echo "Compiling SASS: $TARGET $STYLE"
	sass --version
	sass --style $STYLE $UPDATE $TARGET:$TARGET --load-path light light/modules
	echo "sass --style $STYLE $UPDATE $TARGET:$TARGET --load-path light light/modules"
	echo "done"
}

control_c () {
	exit 130;
}

while getopts ":fhw" opt; do
  case $opt in
    f)
      UPDATECMD="--update --force"
      ;;
    w)
      UPDATECMD="--watch"
      ;;
    [h?])
      echo "Usage: $0 [-fhw] <theme>"
      echo "  f      Update and recompile all Sass files, even if the CSS file is newer"
      echo "  h      Show this message"
      echo "  w      Watch theme for changes"
      exit 1
      ;;
  esac
done

shift $((OPTIND-1))
THEMES=$1

# handle ctrl-c
trap control_c SIGINT

# Add themes to compile from given path
doCompile ".";
searchThemes $THEMES;
echo "DONE"
