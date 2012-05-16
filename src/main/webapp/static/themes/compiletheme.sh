#!/bin/sh
#
# compilescript for openolat-SASS-theme
# can be included into eclipse-project-build (project-properties --> "Builders")
#

style=compressed
#style=compact

echo "compiling SASS: $1 $style"
sass --version

sass --style $style --update $1/layout.scss:$1/layout.css --load-path openolat openolat/all openolat/all/modules openolat/print openolat/mobile

echo "done"