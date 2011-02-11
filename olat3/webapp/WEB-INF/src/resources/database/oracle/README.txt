Oracle Database Support
=======================

The database scripts in this directory are intended to support the
creation of an Oracle 10g (incl. XE) database in a manner consistent
with the OLAT Maven database setup goal and to produce a simple
database instance appropriate for evaluation and/or small site
operations.  

The following list is indicative of the simplifications made:

   * Administrative users (SYS, SYSTEM) are created as "joe" accounts
	 i.e., username and password are the same;
   * All database files (tablespace, redo logs, control files etc.) are
     stored in a common directory tree;
   * The database files has been dimensioned in part with Oracle XE in 
     mind and are therefore rather limiting;
   * Automatic undo management and memory management have been enabled.

Accordingly this default database setup should not be considered suitable
for large data sets (e.g., more than 3-4GB of OLAT data) or for use in
production (i.e., where one might expect an OFA-compliant layout or the
use of multiplexed files etc.)

Since all OLAT data is maintained within a single tablespace you may 
wish to consider creating your own production database (e.g., using DBCA) 
and deploy the OLAT tablespace into that database using the Maven goal.

Creating a Database
===================

It is assumed that you already have an Oracle database product installed.
OLAT has been tested with Oracle 10g (Enterprise and XE) using the JDBC
driver found in the Maven POM (please ensure that your container does 
not impose an alternate JDBC driver!)

Before you can create an Oracle instance you must first create the 
underlying database (i.e., parameters file, datafiles, redo logs etc.)
Note that when executing the scripts cited below certain errors are
considered "normal" and may be safely ignored (e.g., dropping a table
that does not already exist etc.)

These steps need only be performed once prior to installing OLAT:

1) Start your Oracle software (or service) and ensure that all
   components are operational (e.g., TNS, listener etc.) and that
   you have access to the "oracle" user who should be in the "dba"
   group (as per your site policy.)

2) Set your ORACLE_SID environment variable to the database name 
   you are using in your olat.local.properties file e.g.,

   export ORACLE_SID=OLAT

   Note: if you are using XE it may be necessary to edit the
   startup script /etc/init.d/oracle-xe replacing the ORACLE_SID
   therein and restart the database software.

3) Copy the initOLAT.ora into your $ORACLE_HOME/dbs directory

4) Create the common data directory as cite in your olat.local.properties
   the necessary subdirectories.  For example if your common data dir
   is /usr/lib/oracle/OLAT then you should issue the following as 
   the "oracle" user:

   mkdir -p /var/lib/oracle/OLAT/{adump,bdump,cdump,dbs,dpdump,pfile,udmp}

5) Connect to your local instance and create the database (note that
   the SYS and SYSTEM accounts have SYS and SYSTEM passwords respectively
   - this is something you may wish to change after installation)

   sqlplus /nolog
   SQL>connect sys as sysdba
   SQL>shutdown immediate
   SQL>startup nomount
   SQL>@/path/to/createDatabase.sql
   SQL>@?/rdbms/admin/catalog.sql
   SQL>@?/rdbms/admin/catproc.sql
   SQL>connect system
   SQL>@?/sqlplus/admin/pupbld.sql
   SQL>connect sys as sysdba
   SQL>shutdown immediate
   SQL>startup
   SQL>@/path/to/createTablespace.sql
   SQL>create spfile from pfile='/path/to/initOLAT.ora'

   Note: if you are using Oracle XE then it is only possible to host
   one instance per server and you must therefore remove the existing
   XE database first using the following commands

   sqlplus /nolog
   SQL>connect sys as sysdba
   SQL>shutdown immedate
   SQL>startup mount exclusive restrict
   SQL>drop database;
   SQL>quit

6) Create a password file for the OLAT DBA account (note that this account
   will NOT use the same password as the SYS/SYSDBA account!) using the
   "oracle" user, i.e.,

   orapwd file=$ORACLE_HOME/dbs/orapwOLAT password=secret entries=10 force=y

   where /var/lib/oracle/OLAT is your data directory and "secret" is the same 
   password used for db.admin.pass in your olat.local.properties file and
   the "file" is set to the remote login password file path used in your
   initOLAT.ora file.  

   Since it is very likely that you will wish to have administrative access
   via SQL*Net for users outside the "dba" group you should first ensure
   that you are using an exclusive remote login password file by issuing

   SQL>show parameter password

   If this is the case (e.g., you are using the supplied initOLAT.ora file)
   then issue the following to create a suitable OLATDBA user:

   sqlplus /nolog
   SQL>connect sys as sysdba
   SQL>create user olatdba identified by secret
   SQL>grant sysdba to olatdba
   SQL>grant create session to olatdba

   where "secret" is as described above.  This is the user/password that 
   should be used in the db.admin.* properties of the olat.local.properties
   file.  You can verify the above configuration by issuing:

   SQL>select * from v$pwfile_users;


7) Verify that your instance is visible to the local listener
   by issuing:

   lsnrctl services

   and check that your output includes something similar to
   the following:

   Service "OLAT" has 1 instance(s).
     Instance "OLAT", status READY, has 1 handler(s) for this service...
       Handler(s):
         "DEDICATED" established:1 refused:0 state:ready
            LOCAL SERVER

   If this is not so then wait a couple of minutes and try again or issue

   sqlplus /nolog
   SQL>connect sys as sysdba
   SQL>alter system register

8) This step is optional, but advisable, and is required if you intend
   to use the Oracle client "sqlplus".

   Edit $ORCLE_HOME/network/admin/tnsnames.ora to ensure it contains
   an entry similar to the following:

   OLAT =
     (DESCRIPTION =
       (ADDRESS = (PROTOCOL = TCP)(HOST = localhost)(PORT = 1521))
       (CONNECT_DATA =
         (SERVER = DEDICATED)
         (SERVICE_NAME = OLAT)
       )
     )

   and verify that your instance is TNS visible by issuing the following:

   tnsping OLAT

   and check that your output includes something similar to
   the following:

   Used TNSNAMES adapter to resolve the alias
   Attempting to contact (DESCRIPTION = (ADDRESS = (PROTOCOL = TCP)(HOST =
   localhost)(PORT = 1521)) (CONNECT_DATA = (SERVER = DEDICATED) (SERVICE_NAME =
   OLAT)))
   OK (0 msec)

The underlying database is now complete and you may create the
OLAT instance using the standard Maven mechanism (i.e., "mvn install"
or "mvn olat:dbinstall") as described in the OLAT documentation.

Enjoy!
The OLAT Team
