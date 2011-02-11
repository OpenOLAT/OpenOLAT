#!/bin/bash
#
# Run this script when you add a new YAML version. In brasato, we add a "b_" prefix to all
# CSS classes and ID selectors to prevent clashes with content related CSS classes.
#
# Florian Gnägi, frentix GmbH
# http://www.frentix.com
#############################

find * -type f -name "*.css" -exec sed -i .orig "s/#page/#b_page/g" {} \;
find * -type f -name "*.css" -exec sed -i .orig "s/#header/#b_header/g" {} \;
find * -type f -name "*.css" -exec sed -i .orig "s/#nav/#b_nav/g" {} \;
find * -type f -name "*.css" -exec sed -i .orig "s/#topnav/#b_topnav/g" {} \;
find * -type f -name "*.css" -exec sed -i .orig "s/#current/#b_current/g" {} \;
find * -type f -name "*.css" -exec sed -i .orig "s/#main/#b_main/g" {} \;
find * -type f -name "*.css" -exec sed -i .orig "s/#col/#b_col/g" {} \;
find * -type f -name "*.css" -exec sed -i .orig "s/#submenu/#b_submenu/g" {} \;
find * -type f -name "*.css" -exec sed -i .orig "s/#title/#b_title/g" {} \;
find * -type f -name "*.css" -exec sed -i .orig "s/#active/#b_active/g" {} \;
find * -type f -name "*.css" -exec sed -i .orig "s/#footer/#b_footer/g" {} \;
find * -type f -name "*.css" -exec sed -i .orig "s/\.hide/\.b_hide/g" {} \;
find * -type f -name "*.css" -exec sed -i .orig "s/\.clearfix/\.b_clearfix/g" {} \;
find * -type f -name "*.css" -exec sed -i .orig "s/\.floatbox/\.b_floatbox/g" {} \;
find * -type f -name "*.css" -exec sed -i .orig "s/\.slidebox/\.b_slidebox/g" {} \;
find * -type f -name "*.css" -exec sed -i .orig "s/#ie_clearing/#b_ie_clearing/g" {} \;
find * -type f -name "*.css" -exec sed -i .orig "s/\.sub/\.b_sub/g" {} \;
find * -type f -name "*.css" -exec sed -i .orig "s/\.c25/\.b_c25/g" {} \;
find * -type f -name "*.css" -exec sed -i .orig "s/\.c33/\.b_c33/g" {} \;
find * -type f -name "*.css" -exec sed -i .orig "s/\.c38/\.b_c38/g" {} \;
find * -type f -name "*.css" -exec sed -i .orig "s/\.c50/\.b_c50/g" {} \;
find * -type f -name "*.css" -exec sed -i .orig "s/\.c66/\.b_c66/g" {} \;
find * -type f -name "*.css" -exec sed -i .orig "s/\.c62/\.b_c62/g" {} \;
find * -type f -name "*.css" -exec sed -i .orig "s/\.c75/\.b_c75/g" {} \;
find * -type f -name "*.css" -exec sed -i .orig "s/\.skip/\.b_skip/g" {} \;
find * -type f -name "*.css" -exec sed -i .orig "s/\.hideme/\.b_hideme/g" {} \;
find * -type f -name "*.css" -exec sed -i .orig "s/\.print/\.b_print/g" {} \;
find * -type f -name "*.css" -exec sed -i .orig "s/\.trans/\.b_trans/g" {} \;
find * -type f -name "*.css" -exec sed -i .orig "s/\.bg/\.b_bg/g" {} \;
