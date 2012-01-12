#!/bin/sh
#
# compilescript for openolat-SASS-theme
# can be included into eclipse-project-build (project-properties --> "Builders")
#

sass --version
sass --style extended  --update --load-path openolat openolat/all openolat/all/modules openolat/print openolat/mobile .
