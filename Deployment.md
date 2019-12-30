# Deployment of OpenOlat (Version 14)

This README focuses on deployment of an existing war-File within a current Linux-OS with current OpenOlat. 
To facilitate installation and maintenance, Prerequisites shipped by the distribution will be used where ever applicable. 
Tested with OpenSuSE LEAP 15.1. 
   
## Prerequisites

* Tomcat 7 or 8
* PostgreSQL 9.4, 10.x
* JRE 8
* OpenOlat Version 14.x


## Prepare Data Dir

* Download OpenOlat war-File into <b>usersDownloadDirectory</b>.
* Extract the war-Files content to a new directory. We need some of the war's content later on. 

```bash 
> wget http://www.openolat.org/fileadmin/downloads/releases/openolat_1416.war
> unzip openolat_1416.war -d openolat
```
* Install Tomcat, OpenJRE and PostgreSQL via Distributions Package Manager.
* Create Data-Directory for OpenOlat either as root or with sudo

```bash 
> sudo mkdir /opt/olatdata
```

* Copy files needed for installation from extracted war into /opt/olatdata
  
```bash 
> sudo mkdir /opt/olatdata/install
> sudo cp openolat/WEB-INF/classes/serviceconfig/olat.properties /opt/olatdata/install/olat.local.properties
> sudo cp -R openolat/WEB-INF/classes/database/* /opt/olatdata/install/
> sudo chown -R /opt/olatdata
```

## Prepare PostgreSQL

* As we do not deploy the openolat LMS with its own user and within the users home directory we have to prepare the postgreSQL Database to 
allow login with a secure password first. Via root or sudo login as postgres and modify pg_hba.conf and postgres.conf  

```bash 
> sudo su 
# su postgres
> cd ~
> vi data/pg_hba.cf
```

```
# TYPE  DATABASE        USER            ADDRESS                 METHOD
# Connect from tomcat
host   olat            olat             127.0.0.1/32            scram-sha-256
# Connect from psql
local  olat            olat                                     scram-sha-256
...
```

```bash
> vi data/postgres.conf
```
```
# set encryption to current state
password_encryption = scram-sha-256             # md5 or scram-sha-256
...
```

* Restart PostgreSQL

```bash 
> sudo service postgresql stop
> sudo service postgresql start
```
 
* As user postgres set up database and user for openolat. Use the psql client for this. 

```bash 
> sudo su 
# su postgres
> psql
=# create role olat with login password '*******';
=# create database olat with owner olat;
=# \q
>
```

* With your default user check if you are able to login with the olat role.

```bash 
> psql -d olat -U olat 
olat=>
olat=> \q
>
```

* Using psql with role olat to setup the OpenOlat database structure

```bash 
olat=> \i /opt/olatdata/install/postgresql/setupDatabase.sql
olat=> \q
>
```

## Prepare Tomcat 8

Tomcat has changed basically how it uses Memory. Therefore most of parameters given for organizing Memory within Tomcat 7 will not work as expected. This will have no effect under normal circumstances as Tomcat 8 is able to acquire Memory dynamically if required. 
<b>Nevertheless, if using Tomcat 8 OpenOlat will refuse to start due to the lack of appropriate cache space.</b> To solve this issue you have to enhance the cache size provided by Tomcat manually. Within Tomcat's context.xml set cacheSize

```bash 
<Context>
    <Resources cacheMaxSize="51200" />
	...
</Context>
```

## Setup Container Configuration  

* Edit olat.local.properties in the olatdata dir according your configuration. 
* Stop Tomcat and copy olat.local.properties into the unpacked olat dir.

```bash 
> sudo service tomcat stop
> sudo cp /opt/olatdata/install/olat.local.properties /TOMCAT_HOME/webapps/openolat/WEB-INF/classes/olat.local.properties
> sudo service tomcat start
```
  




 
