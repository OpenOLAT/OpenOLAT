##
## This script checks out olat3_i18n and olatcore_i18n, build jar files from them and copies the jars into the olat3/webapp/WEB-INF/lib directory
##

#!/bin/sh

echo "NIGHTLY olatcore_i18n BUILD START..." 
# run as user nightly

export CVSROOT=:ext:nightly@cvs.olat.org:/usr/local/cvs
export CVS_RSH=ssh

echo change dir to /usr/local/opt/nightly 
cd /usr/local/opt/nightly

# echo delete existing olatcore_i18n
rm -R olatcore_i18n

# echo CVS checkout
echo CVS update olatcore_i18n
/usr/bin/cvs co olatcore_i18n

# echo Change to dir olatcore_i18n
cd olatcore_i18n

echo Maven clean package
/usr/local/opt/maven/bin/mvn clean package
echo Maven package DONE

echo Change to dir olat3/webapp/WEB-INF/lib
cd /usr/local/opt/nightly/olat3/webapp/WEB-INF/lib

echo CVS update of olatcore_i18n-JAR
/usr/bin/cvs upd -dP olatcore_i18n_all-SNAPSHOT.jar

echo Copy core_i18n-JAR
cp /usr/local/opt/nightly/olatcore_i18n/target/olatcore_i18n_all-SNAPSHOT.jar /usr/local/opt/nightly/olat3/webapp/WEB-INF/lib/olatcore_i18n_all-SNAPSHOT.jar

echo CVS checkin olatcore-JAR
/usr/bin/cvs ci -m'Nightly olatcore_i18n update' olatcore_i18n_all-SNAPSHOT.jar

echo Change to dir /usr/local/home/nightly/
cd /usr/local/home/nightly	

echo "NIGHTLY olatcore_i18n BUILD FINISHED"

## now the same for olat3_i18n

echo "NIGHTLY olat3_i18n BUILD START..." 
# run as user nightly

export CVSROOT=:ext:nightly@cvs.olat.org:/usr/local/cvs
export CVS_RSH=ssh

echo change dir to /usr/local/opt/nightly 
cd /usr/local/opt/nightly

# echo delete existing olat3_i18n
rm -R olat3_i18n

# echo CVS checkout
echo CVS update olat3_i18n
/usr/bin/cvs co olat3_i18n

# echo Change to dir olat3_i18n
cd olat3_i18n

echo Maven clean package
/usr/local/opt/maven/bin/mvn clean package
echo Maven package DONE

echo Change to dir olat3/webapp/WEB-INF/lib
cd /usr/local/opt/nightly/olat3/webapp/WEB-INF/lib

echo CVS update of olat3_i18n-JAR
/usr/bin/cvs upd -dP olat3_i18n_all-SNAPSHOT.jar

echo Copy core_i18n-JAR
cp /usr/local/opt/nightly/olat3_i18n/target/olat3_i18n_all-SNAPSHOT.jar /usr/local/opt/nightly/olat3/webapp/WEB-INF/lib/olat3_i18n_all-SNAPSHOT.jar

echo CVS checkin olat3-JAR
/usr/bin/cvs ci -m'Nightly olat3_i18n update' olat3_i18n_all-SNAPSHOT.jar

echo Change to dir /usr/local/home/nightly/
cd /usr/local/home/nightly	

echo "NIGHTLY olat3_i18n BUILD FINISHED"

