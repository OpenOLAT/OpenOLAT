#!/bin/bash
# countdown for shutting down tomcat

# To find the token for this olatinstance search in the table
# o_property for a property that has the category "_o3_" and
# the name "maintenanceMessageToken".
token="XxXxXxXx"

# the url of this olatinstance
url="https://www.myolat.com"

/usr/bin/wget --no-check-certificate --bind-address=127.0.0.1 --spider "$url/olat/admin.html?cmd=setmaintenancemessage&token=$token&msg=In+10+Minuten+findet+das+taegliche+Datenbackup+statt+(Ausfallzeit+ca.+20+Minuten).+Bitte+loggen+Sie+sich+aus."
sleep 60
/usr/bin/wget --no-check-certificate --bind-address=127.0.0.1 --spider "$url/olat/admin.html?cmd=setmaintenancemessage&token=$token&msg=In+9+Minuten+findet+das+taegliche+Datenbackup+statt+(Ausfallzeit+ca.+20+Minuten).+Bitte+loggen+Sie+sich+aus."
sleep 60
/usr/bin/wget --no-check-certificate --bind-address=127.0.0.1 --spider "$url/olat/admin.html?cmd=setmaintenancemessage&token=$token&msg=In+8+Minuten+findet+das+taegliche+Datenbackup+statt+(Ausfallzeit+ca.+20+Minuten).+Bitte+loggen+Sie+sich+aus."
sleep 60
/usr/bin/wget --no-check-certificate --bind-address=127.0.0.1 --spider "$url/olat/admin.html?cmd=setmaintenancemessage&token=$token&msg=In+7+Minuten+findet+das+taegliche+Datenbackup+statt+(Ausfallzeit+ca.+20+Minuten).+Bitte+loggen+Sie+sich+aus."
sleep 60
/usr/bin/wget --no-check-certificate --bind-address=127.0.0.1 --spider "$url/olat/admin.html?cmd=setmaintenancemessage&token=$token&msg=In+6+Minuten+findet+das+taegliche+Datenbackup+statt+(Ausfallzeit+ca.+20+Minuten).+Bitte+loggen+Sie+sich+aus."
sleep 60
/usr/bin/wget --no-check-certificate --bind-address=127.0.0.1 --spider "$url/olat/admin.html?cmd=setmaintenancemessage&token=$token&msg=In+5+Minuten+findet+das+taegliche+Datenbackup+statt+(Ausfallzeit+ca.+20+Minuten).+Bitte+loggen+Sie+sich+aus."
sleep 60
/usr/bin/wget --no-check-certificate --bind-address=127.0.0.1 --spider "$url/olat/admin.html?cmd=setmaintenancemessage&token=$token&msg=In+4+Minuten+findet+das+taegliche+Datenbackup+statt+(Ausfallzeit+ca.+20+Minuten).+Bitte+loggen+Sie+sich+aus."
sleep 60
/usr/bin/wget --no-check-certificate --bind-address=127.0.0.1 --spider "$url/olat/admin.html?cmd=setmaintenancemessage&token=$token&msg=In+3+Minuten+findet+das+taegliche+Datenbackup+statt+(Ausfallzeit+ca.+20+Minuten).+Bitte+loggen+Sie+sich+aus."
sleep 60
/usr/bin/wget --no-check-certificate --bind-address=127.0.0.1 --spider "$url/olat/admin.html?cmd=setmaintenancemessage&token=$token&msg=In+2+Minuten+findet+das+taegliche+Datenbackup+statt+(Ausfallzeit+ca.+20+Minuten).+Bitte+loggen+Sie+sich+aus."
sleep 60
/usr/bin/wget --no-check-certificate --bind-address=127.0.0.1 --spider "$url/olat/admin.html?cmd=setmaintenancemessage&token=$token&msg=In+wenigen+Augenblicken+findet+das+taegliche+Datenbackup+statt+(Ausfallzeit+ca.+20+Minuten).+Bitte+loggen+Sie+sich+aus."
sleep 60