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
export SASS_PATH=.

# Configuration
STYLE=compressed
#STYLE=compact

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

# Helper method to split files with too many selectors for ie <= 9
ie9ify () {
	css="$TARGET/theme.css"
	[ -e $css  ] && { printf "ie9ify $TARGET:\n  "; blessc --no-imports $css "$TARGET/theme_ie_completions.css"; rm "$TARGET/theme_ie_completions-blessed1.css"; }
	echo "  done"
}

# Helper method to compile the theme
doCompile () {
	TARGET=$1
	if [ $1 = "."  ];
	then
		TARGET="light"
	fi
	echo "Compiling SASS: $TARGET $STYLE"
	sass --version
	sass --style $STYLE --no-cache --update $TARGET:$TARGET --load-path light light/modules
	echo "sass --style $STYLE --update $TARGET:$TARGET --load-path light light/modules"
	[ $bless -eq 0 ] && ie9ify $TARGET
	echo "done"
}

# check for blessc command needed for ie9 optimizations
command -v blessc >/dev/null 2>&1
bless=$?
[ $bless -ne 0 ] && echo >&2 "Install blessc to optimize css for ie <= 9 (npm install -g bless)"

# Add themes to compile from given path
doCompile ".";
searchThemes $1;
echo "DONE"
