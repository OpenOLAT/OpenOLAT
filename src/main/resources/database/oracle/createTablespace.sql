CREATE TABLESPACE OLATTBS
	DATAFILE '${db.oracle.common.data.dir}/${db.name}/${db.name}.dbf'
		SIZE 1024M AUTOEXTEND ON NEXT 16M MAXSIZE 3072M;
