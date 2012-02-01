#!/bin/sh
#
# compilescript for openolat-SASS-theme
# can be included into eclipse-project-build (project-properties --> "Builders")
#

style=compressed

echo "compiling SASS: $style"
sass --version

sass --style $style --update --load-path openolat openolat/all openolat/all/modules openolat/print openolat/mobile .

sass --style $style  --update --load-path openolat openolat/all openolat/all/modules openolat/print openolat/mobile hfgs

: '
sass --style $style  --update --load-path openolat openolat/all openolat/all/modules openolat/print openolat/mobile bzg
sass --style $style  --update --load-path openolat openolat/all openolat/all/modules openolat/print openolat/mobile zag
sass --style $style  --update --load-path openolat openolat/all openolat/all/modules openolat/print openolat/mobile kantiwohlen
sass --style $style  --update --load-path openolat openolat/all openolat/all/modules openolat/print openolat/mobile olatpro
sass --style $style  --update --load-path openolat openolat/all openolat/all/modules openolat/print openolat/mobile business
sass --style $style  --update --load-path openolat openolat/all openolat/all/modules openolat/print openolat/mobile pedsibd
'
echo "done"


