-- Assessment mode
alter table o_as_mode_course add a_safeexambrowserconfig_xml clob;
alter table o_as_mode_course add a_safeexambrowserconfig_plist clob;
alter table o_as_mode_course add a_safeexambrowserconfig_pkey varchar(255);
alter table o_as_mode_course add a_safeexambrowserconfig_dload number default 1 not null;
