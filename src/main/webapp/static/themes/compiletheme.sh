#!/bin/sh
#
# compilescript for openolat-SASS-theme
# can be included into eclipse-project-build (project-properties --> "Builders")
#

echo "compiling SASS..."
sass --version

#sass --style extended  --update --load-path openolat openolat/all openolat/all/modules openolat/print openolat/mobile .

#sass --style extended  --update --load-path openolat openolat/all openolat/all/modules openolat/print openolat/mobile bzg
#sass --style extended  --update --load-path openolat openolat/all openolat/all/modules openolat/print openolat/mobile zag
sass --style extended  --update --load-path openolat openolat/all openolat/all/modules openolat/print openolat/mobile kantiwohlen



echo "done"
