#!/bin/sh

echo "NIGHTLY CORE BUILD START..." 
# run as user nightly

export CVSROOT=:ext:nightly@cvs.olat.org:/usr/local/cvs
export CVS_RSH=ssh

echo change dir to /usr/local/opt/nightly 
cd /usr/local/opt/nightly

# echo delete existing olatcore
rm -R olatcore

# echo CVS checkout
echo CVS update olatcore
/usr/bin/cvs co olatcore

# echo Change to dir olatcore
cd olatcore

echo Maven clean package
/usr/local/opt/maven/bin/mvn clean package
#  1.3 Copy JAR to olat3 project
#  1.4 Allfaellige JUnit-Tests laufen lassen (spaeter)
#  1.5 CVS checkin olatcore.jar into olat3
#      First update olatcore-jar from cvs, perhaps it changed
echo Maven package DONE

echo Change to dir olat3/webapp/WEB-INF/lib
cd /usr/local/opt/nightly/olat3/webapp/WEB-INF/lib

echo CVS update of core-JAR
/usr/bin/cvs upd -dP olatcore-1.0-SNAPSHOT.jar

echo Copy core-JAR
cp /usr/local/opt/nightly/olatcore/target/olatcore-1.0-SNAPSHOT.jar /usr/local/opt/nightly/olat3/webapp/WEB-INF/lib/olatcore-1.0-SNAPSHOT.jar

echo CVS checkin core-JAR
/usr/bin/cvs ci -m'Nightly olatcore update' olatcore-1.0-SNAPSHOT.jar

echo Change to dir /usr/local/home/nightly/
cd /usr/local/home/nightly	

echo "NIGHTLY CORE BUILD FINISHED"

