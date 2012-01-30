#!/bin/sh
#
# compilescript for openolat-SASS-theme
# can be included into eclipse-project-build (project-properties --> "Builders")
#

style=compressed

echo "compiling SASS: $style"
sass --version

sass --style $style --update --load-path openolat openolat/all openolat/all/modules openolat/print openolat/mobile .



echo "done"

