alter table o_gta_task modify g_taskname varchar(1024);

update o_ac_method set creationdate=now() where creationdate is null;
update o_ac_method set lastmodified=creationdate where lastmodified is null;