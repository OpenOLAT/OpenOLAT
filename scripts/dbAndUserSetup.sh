#!/bin/bash
#################################################################################
# Initial database setup script: will generate db user and an openolat database #
# 11. July 2004 Florian Gnaegi                                                  #
#################################################################################

echo "This script will create a database user and a database for OpenOLAT on your MySQL server. Do you want to continue? (y|n)"
read Y
if [ "$Y" != "y" ]; then
	echo byby
	exit	
fi

# collect user dbname, username, password
echo Enter your OpenOLAT database name:
read OLATDB
echo Enter your OpenOLAT database user:
read OLATUSER
echo Enter the password for database user $OLATUSER:
read OLATPWD

# create mysql db query
QUERY="CREATE DATABASE $OLATDB; GRANT ALL PRIVILEGES ON $OLATDB.* TO '$OLATUSER'@'localhost' IDENTIFIED BY '$OLATPWD';"

# executing query
echo Generating now the OpenOLAT database user $OLATUSER with the password $OLATDB that has access to the database $OLATDB 
echo You must now enter the MySQL root user password:
mysql -u root -p mysql -e "$QUERY"
echo "User generated. Try login now with 'mysql -u $OLATUSER -p $OLATDB'"
