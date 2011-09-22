CREATE DATABASE ${db.name}
	MAXINSTANCES 1
	USER SYS IDENTIFIED BY SYS
	USER SYSTEM IDENTIFIED BY SYSTEM
	LOGFILE 
		GROUP 1 ('${db.oracle.common.data.dir}/${db.name}/redo1.log') SIZE 16M,
        	GROUP 2 ('${db.oracle.common.data.dir}/${db.name}/redo2.log') SIZE 16M,
        	GROUP 3 ('${db.oracle.common.data.dir}/${db.name}/redo3.log') SIZE 16M
	CHARACTER SET utf8
	NATIONAL CHARACTER SET utf8
	EXTENT MANAGEMENT LOCAL
	CONTROLFILE REUSE
	DATAFILE '${db.oracle.common.data.dir}/${db.name}/system.dbf' 
		SIZE 128M AUTOEXTEND ON NEXT 8M MAXSIZE 256M 
	sysaux DATAFILE '${db.oracle.common.data.dir}/${db.name}/sysaux.dbf'
		SIZE 128M AUTOEXTEND ON NEXT 8M MAXSIZE 256M
	DEFAULT TEMPORARY TABLESPACE temp
		TEMPFILE '${db.oracle.common.data.dir}/${db.name}/temp.dbf'
			SIZE 16M AUTOEXTEND ON NEXT 8M MAXSIZE 128M
	UNDO TABLESPACE undotbs
		DATAFILE '${db.oracle.common.data.dir}/${db.name}/undotbs.dbf'
			SIZE 16M AUTOEXTEND ON NEXT 8M MAXSIZE 64M;
