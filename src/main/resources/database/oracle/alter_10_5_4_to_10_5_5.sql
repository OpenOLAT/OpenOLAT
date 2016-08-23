alter table o_gta_task modify g_taskname varchar2(1024 char);

update o_ac_method set creationdate=systimestamp where creationdate is null;
update o_ac_method set lastmodified=creationdate where lastmodified is null;