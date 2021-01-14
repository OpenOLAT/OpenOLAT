# OpenOlat

<a href="https://www.openolat.org"><img src="src/main/webapp/static/images/openolat/openolat_logo_claim_small_rgb.png" align="left"></a>

 **OpenOlat** is a web-based e-learning platform for teaching, learning, assessment and communication, an LMS, a learning management system. OpenOlat impresses with its simple and intuitive operation.

A sophisticated modular toolkit provides course authors with a wide range of didactic possibilities. Each OpenOlat installation can be individually extended, adapted to organizational needs, and integrated into existing IT infrastructures. The architecture is designed for minimal resource consumption, scalability and security in order to guarantee high system reliability.

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Table of Contents

1. [Licensing](#licensing)
2. [Ressources](#ressources)
3. [Community](#being-a-community-member)
4. [Developers](#developers)
    * [Setting up OpenOlat in Eclipse](#setting-up-openolat-in-eclipse)
    * [Compress javascript and CSS](#compress-javascript-and-css)
    * [REST API](#rest-api)
    * [Automated tests](#automated-tests)
5. [Supported by](#supported-by)

## Licensing

With regard to licensing and copyright please refer to the file [LICENSE](LICENSE) and [NOTICE.TXT](NOTICE.TXT)

## Ressources 

* [Installation](https://www.openolat.com/fileadmin/adminwiki/_START_.html)
* [About OpenOlat](https://www.openolat.com)
* [Mailing list](https://groups.google.com/forum/#!forum/openolat)
* [Issues](https://jira.openolat.org/)
* [OpenOlat for the community](https://community.openolat.org) An OpenOlat instance dedicated to the community


## Being a community member

We strongly suggest to participate in the OpenOlat community membership program. 
Even though this software is free and open source, the development and management
has to be funded by someone. If you like what we do and want the product to be
maintained and developed in the long run you should consider purchasing a membership:
[Partner program](https://www.openolat.com/open-source/partner-program/?lang=en).

## Developers

### Setting up OpenOlat in Eclipse

This is an installation guide for developers.

#### Preconditions
* Check that you are using maven 3.1 or higher (mvn -V)
* Check that you have the git plugin installed in eclipse
* Check that you have git installed
* MySQL 5.6 or greater or PostreSQL 9.4

#### 1. In Eclipse

Create a repository location (https://github.com/OpenOLAT/OpenOLAT.git) and
clone the repo. Right click to clone the repository into your workspace.

If M2_REPO Eclipse variable is missing in Eclipse then execute in terminal:

```bash
mvn -Declipse.workspace=<location of your workspace> eclipse:configure-workspace
```

In a terminal, create the eclipse project and classpath settings by running:

```bash
mvn eclipse:clean eclipse:eclipse
```

Refresh the project. 

You need now to create an olat.local.properties file somewhere. Copy the
`olat.local.properties.sample` to `olat.local.properties` in the project root folder, adjust
the file to match your setup. See the comments in the file header for more configuration
options. Right click on the file `src/main/java/olat.local.properties` in our eclipse
project to open the file properties and link it to your own olat.local.properties
you created before. 
  
Make sure the project compiled without errors. Warnings are ok. If the project did not
compile, you have to fix the problems before you proceed. See [Troubleshooting](#troubleshooting)
section below.
      
#### 2. Setting up the database

Prepare database permissions and initialize the database.

*For PostgreSQL*: please check their PostgreSQL manual how to create a user and database.
 
*For MySQL*: create a user 'openolat' and a database 'openolat'

```sql
CREATE DATABASE IF NOT EXISTS openolat;
GRANT ALL PRIVILEGES ON openolat.* TO 'openolat' IDENTIFIED BY 'openolat';
UPDATE mysql.user SET HOST='localhost' WHERE USER='openolat' AND HOST='%';
FLUSH PRIVILEGES;
```

The time zone needs to be set if you don't already defined it.

```sql
SET GLOBAL time_zone = 'Europe/Zurich';
```
 
Write the OpenOlat database schema to the OpenOlat database. Example for MySQL:

```bash
mysql -u openolat -p openolat < src/main/resources/database/mysql/setupDatabase.sql
```

Optional: if you want to run the jUnit tests, make sure you also create and initialize the
test database that you configured in `src/test/profile/mysql/olat.local.properties` for
MySQL or `src/test/profile/postgresql/olat.local.properties` for PostgreSQL.


*For Oracle*: 

Oracle support is experimental. The database schema is available and 
updated for historic reason, however running OpenOlat with Oracle is 
largely untested. Do not use it for production before you tested the 
entire application. We are interested in adding Oracle to our list of
fully supported and recommended database, contact us if you want to 
sponsor this compatibility. 


#### 3. Setting up the Tomcat server in Eclipse

Setup a tomcat server by clicking on OpenOlat -> Run As -> "Run on Server". The
"Run on Server" wizard will pop up and you define define a new server. Look for
Apache -> Tomcat v8.0.

Add openolat as web application in the step "Add and remove" and click finish.

Double click the newly created server and increase the timeout to something like 180s.

Open the launch configuration of the server, select the tab Arguments and add these
arguments to the VM arguments:

```
-XX:+UseG1GC -XX:+UseStringDeduplication -Xms256m -Xmx1024m -Djava.awt.headless=true
```

Open the generated server.xml file and manually set the following parameters: 
* In the "Context" element set parameter reloadable="false" 
* In all "Connector" elements set parameter URIEncoding="UTF-8"

Option: to use the application server database connection pool configure a jdbc data
resource in the "Context" element, set db.source=jndi in the olat.local.properties with
db.jndi to the name of the data source like this:

```
db.source=jndi
db.jndi=java:comp/env/jdbc/OpenOLATDS
```

Next add the resource descriptor to your tomcat context descriptor.

*For MySQL:*

```xml
<Resource auth="Container" driverClassName="com.mysql.cj.jdbc.Driver" type="javax.sql.DataSource"
          maxIdle="4" maxTotal="16" maxWaitMillis="10000"
          name="jdbc/OpenOLATDS"
          password="olat" username="olat"
          url="jdbc:mysql://localhost:3306/openolat?useUnicode=true&amp;characterEncoding=UTF-8&amp;cachePrepStmts=true&amp;cacheCallableStmts=true&amp;autoReconnectForPools=true"
          testOnBorrow="true" testOnReturn="false"
          validationQuery="SELECT 1" validationQueryTimeout="-1"/>
```

*For PostreSQL:*

```xml
<Resource auth="Container" driverClassName="org.postgresql.Driver" type="javax.sql.DataSource"
          maxIdle="4" maxTotal="16" maxWaitMillis="-1"
          name="jdbc/OpenOLATPostgresDS"
          username="postgres" password="postgres"
          url="jdbc:postgresql://localhost:5432/olat"
          testOnBorrow="true" testOnReturn="false"
          validationQuery="SELECT 1" validationQueryTimeout="-1"/>
```

If unsure, set db.source=local to use the bundled data connection pool (not recommended for production)

You can now start the server and open the following URL
[http://localhost:8080/olat](http://localhost:8080/olat) in your favorite browser. You can
log in with user "administrator" and password "openolat".
   
Have fun, give feedback and contribute!

#### Troubleshooting

* OutOfMemoryException: in Eclipse: setup VM arguments by clicking on Run > Run Configurations > Arguments > VM Arguments
  and pasting: `-XX:+UseG1GC -XX:+UseStringDeduplication -Xms256m -Xmx1024m -Djava.awt.headless=true`

* Optional: create an empty olat.local.properties and save it to /yourTomcatDir/lib
  (OpenOlat searches for this file on the classpath and /tomcat/lib is part of it). But it
  should start with just the default config!

* Usually you will get a timeout exception when you start a new OpenOlat. After double clicking
  on the server entry you can increase the timeout for the startup.

* If your tomcat starts very quickly but you cannot access OpenOlat it might be that tomcat did
  not find the OpenOlat context. Right click the server entry and click publish to inform eclipse
  about a new or updated context.

* If you run into problems with classes or resources not found e.g. "ClassNotFoundException" right click your 
  server config and run the "Clean..." Task to republish all resources. Problems comes mostly when switching 
  from eclipse to console and back with command like mvn clean, or eclipse clean and such. You will always get 
  a clean and working environment when you do the following: Eclipse clean, create eclipse settings with launch, 
  Server publish resources and restart OpenOlat.

### Background (optional for further interest)

There is only one spring context for the whole OpenOlat which you can access via
CoreSpringFactory. The context is configured with the files`serviceconfig/olat.properies`
and can be overwritten with `olat.local.properties`. Changes in olat.local.properties are
reflected upon each restart of Tomcat. You can further override OpenOlat settings with
JVM arguments `-Dmy.option=enabled`.

### Compress javascript and CSS

The javascript and CSS files are minified and aggregated. If you make some changes, run the following
command to compress them (execution time ca. 1-2 minutes) and refresh your Eclipse project:

```bash
mvn clean package
mvn compile -Pcompressjs,tomcat
```

### Themes

[Readme](src/main/webapp/static/themes/themes.README)

### REST API 

To read the OpenOlat REST API documentation:
1. start OpenOlat
2. go to Administration -> Core configuration -> REST API
3. Make sure the REST API is enabled
4. Click the documentation link in the admin panel

### Automated tests

#### Preconditions

* Make sure the following ports are not in use (Selenium, Tomcat )
  `14444 / 8080 / 8009 / 8089`

* Make sure you have a MySQL database version 5.6 with the InnoDB as default engine
  or PostgreSQL 9.4 or newer. The server must be at localhost.

* Make sure maven has enough memory. E.g execute the following:

```bash
export MAVEN_OPTS= -Xms512m -Xmx1024m
mvn compile -Pdocumentation,tomcat
```

* Make sure the tmp directory is writable. E.g. execute the following.

```bash
ls -la `printenv TMPDIR`
```

#### Setup (necessary only once)

Setup database users and tables in the pom.xml. The default settings are:

```xml
<test.env.db.name>olattest</test.env.db.name>
<test.env.db.user>olat</test.env.db.user>
<test.env.db.pass>olat</test.env.db.pass>
```

You can override them with -D in the command line.

You need an empty database named olat. The maven command will create and drop
databases automatically but need an existing database to do that. Here we will
explain it with MySQL.

Setup first an user for the database

```sql
CREATE USER 'olat'@'localhost' IDENTIFIED BY 'olat';
```

Create the database:

```sql
CREATE DATABASE IF NOT EXISTS olat;
GRANT ALL PRIVILEGES ON olat.* TO 'olat' IDENTIFIED BY 'olat';
UPDATE mysql.user SET HOST='localhost' WHERE USER='olat' AND HOST='%';
FLUSH PRIVILEGES;
```

Create the real test database, it will set once the permissions:

```sql
CREATE DATABASE IF NOT EXISTS olattest;
GRANT ALL PRIVILEGES ON olattest.* TO 'olat' IDENTIFIED BY 'olat';
FLUSH PRIVILEGES;
```

Initialize the database
```bash
mysql -u olat -p olattest < src/main/resources/database/mysql/setupDatabase.sql
```

#### Execute JUnit tests

The JUnit tests load the framework to execute (execution time ca. 10m)

**For MySQL**

```bash
mvn clean test -Dwith-mysql -Ptomcat
```

The following options are available to configure the database connection:

```bash  
-Dtest.env.db.user=root
-Dtest.env.db.pass=serial
```

**For PostgreSQL**

```bash
mvn clean test -Dwith-postgresql -Ptomcat
```

with the options:

```bash
-Dtest.env.db.postgresql.user=postgres
-Dtest.env.db.postgresql.pass=serial
```

To only run the OpenOlat test suite and exclude the unit tests of QtiWorks, add the following
option:

```bash
-Dtest=org.olat.test.AllTestsJunit4
```

Example:

```bash
mvn clean test -Dwith-postgresql -Dtest.env.db.postgresql.pass=serial -Dtest=org.olat.test.AllTestsJunit4 -Ptomcat
```


**For Oracle**

Setup manually the user and the database schema. Start with a clean setup, drop the user before create a clean new one.


```sql
drop user OLATTEST cascade;
```

Create the user.

```sql
create user OLATTEST identified by "olat00002" temporary tablespace temp default tablespace users;
grant connect to OLATTEST;
grant all privileges to OLATTEST;
```

Setup the schema with setupDatabase.sql for Oracle and run the tests:

```bash
mvn clean test -Dwith-oracle -Dtest.env.db.oracle.pass=olat00002 -Dtest=org.olat.test.AllTestsJunit4 -Ptomcat
```


#### Execute selenium functional tests

The selenium integration tests start the whole web application in Tomcat 8.0. They run with
Google Chrome or Firefox and their WebDrivers will be automatically downloaded (internet connection
needed). The browsers need to be installed the standard way on Mac or Linux.

Execution time ca. 60 - 90m

**For MySQL:**

```bash
mvn clean verify -DskipTests=true -Dwith-mysql -Ptomcat
```

**For PostgreSQL:**

```bash
mvn clean verify -DskipTests=true -Dwith-postgresql -Dtest.env.db.postgresql.pass=serial -Ptomcat
```

Or with Firefox

```bash
mvn clean verify -DskipTests=true -Dwith-postgresql -Dtest.env.webdriver.browser=firefox -Dtest.env.db.postgresql.pass=serial -Ptomcat
```

#### Execute a single selenium functional integration test in Eclipse

First build the application without tests as before

```bash
mvn clean verify -DskipTests=true -DskipSeleniumTests=true -Ptomcat
```

Run single test as JUnit Test in Eclipse

## Supported by

This software is supported by the following tool vendors.

<a href="https://www.yourkit.com"><img src="https://www.yourkit.com/images/yk_logo.png" height="60" width="220"></a>

<a href="https://www.atlassian.com"><img src="https://www.atlassian.com/dam/jcr:93075b1a-484c-4fe5-8a4f-942710e51760/Atlassian-horizontal-blue@2x-rgb.png" height="61" width="481"></a>


