#!/bin/sh
#
# Compilescript for openolat-SASS-theme
# Can be included into eclipse-project-build (project-properties --> "Builders")
# 
# It compiles the whole directory and mostly  all changed files. 
# In the case it does not compile all files, it's helpful to delete the sass-cache (rm -r .sass-cache).
#
# usage:
# ./compiletheme.sh									# compile themes in the current directory
# ./compiletheme.sh /path/to/your/custom/theme		# compile themes in the given directory
#
####

style=compressed
#style=compact

# default to compile the themes in the current directory
directory="."
if [ ! -z $1  ]; then directory=$1; fi

echo "Compiling SASS: $directory $style"
sass --version

sass --style $style --update $directory:$directory --load-path openolat openolat/all openolat/all/modules openolat/print openolat/mobile

echo "done"